param(
    [string]$Version = "1.0.5",
    [string]$ApkPath = "apk\app-v1.0.5-debug.apk",
    [string]$ReleaseTitle = "v1.0.5 - Notification Buttons, Home Pager Swipe & Practice Updates",
    [string]$Changelog = @"
## What's New in v1.0.5
- **Top Bar Notification Buttons**: Quick access blue notification button on Home, Student Info, Learn, and Practice pages.
- **Home Horizontal Pager**: Fluid visual swipe to Student Info page (Page 0) and Learn (Page 2) with back-handler support.
- **Practice Screen Fixes**: Neo-brutalist rounded delete button on MCQs, FlowRow chip wrapping for Exception Handling, and clean bottom sheet header.
- **In-App GitHub Updates**: Live scanner for GitHub Releases and Assets to download and install updates in-app.

### Downloads
- `app-v1.0.5-debug.apk` (Android Debug APK)
"@,
    [string]$Token = $env:GITHUB_TOKEN
)

if (-not $Token) {
    # Check if git credential manager or local env provides it
    $Token = $env:GH_TOKEN
}

if (-not $Token) {
    Write-Error "GitHub Token is required. Set `$env:GITHUB_TOKEN or pass -Token <token>"
    exit 1
}

$repo = "vastavikCodingStuff/vastavikLearning-app"
$tagName = "v$Version"

$headers = @{
    "Authorization" = "Bearer $Token"
    "Accept"        = "application/vnd.github.v3+json"
    "User-Agent"    = "PowerShell"
}

# 1. Check if release already exists
Write-Host "Checking for existing release $tagName on $repo..."
$existingRelease = $null
try {
    $existingRelease = Invoke-RestMethod -Uri "https://api.github.com/repos/$repo/releases/tags/$tagName" -Headers $headers -Method Get -ErrorAction Stop
} catch {
    # Release does not exist yet
}

if ($existingRelease) {
    Write-Host "Release $tagName already exists with ID: $($existingRelease.id)"
    $release = $existingRelease
} else {
    Write-Host "Creating GitHub Release $tagName..."
    $releaseBody = @{
        tag_name         = $tagName
        target_commitish = "main"
        name             = $ReleaseTitle
        body             = $Changelog
        draft            = $false
        prerelease       = $false
    } | ConvertTo-Json -Depth 5

    $release = Invoke-RestMethod -Uri "https://api.github.com/repos/$repo/releases" -Headers $headers -Method Post -Body $releaseBody -ContentType "application/json; charset=utf-8"
    Write-Host "Release created successfully with ID: $($release.id)"
}

# 2. Upload APK Asset
if (Test-Path $ApkPath) {
    $fileName = [System.IO.Path]::GetFileName($ApkPath)
    Write-Host "Uploading asset $fileName ($((Get-Item $ApkPath).Length) bytes)..."

    # Check if asset already uploaded
    $existingAsset = $release.assets | Where-Object { $_.name -eq $fileName }
    if ($existingAsset) {
        Write-Host "Asset $fileName already exists on release, deleting old asset (ID: $($existingAsset.id))..."
        Invoke-RestMethod -Uri "https://api.github.com/repos/$repo/releases/assets/$($existingAsset.id)" -Headers $headers -Method Delete
    }

    $uploadUrl = "https://uploads.github.com/repos/$repo/releases/$($release.id)/assets?name=$fileName"
    $bytes = [System.IO.File]::ReadAllBytes((Resolve-Path $ApkPath).Path)

    $uploadHeaders = @{
        "Authorization" = "Bearer $Token"
        "Accept"        = "application/vnd.github.v3+json"
        "Content-Type"  = "application/vnd.android.package-archive"
        "User-Agent"    = "PowerShell"
    }

    $asset = Invoke-RestMethod -Uri $uploadUrl -Headers $uploadHeaders -Method Post -Body $bytes
    Write-Host "Asset uploaded successfully!"
    Write-Host "Asset Download URL: $($asset.browser_download_url)"
} else {
    Write-Error "APK file not found at $ApkPath"
}

Write-Host "Release process finished!"
Write-Host "Release HTML: $($release.html_url)"
