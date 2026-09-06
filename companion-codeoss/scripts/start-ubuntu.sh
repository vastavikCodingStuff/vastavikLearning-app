#!/bin/sh
# Vastavik CodeOSS - Minimal Ubuntu Terminal & Code Server Daemon
# Stripped to the bare minimum: No GUI, No X11, No Systemd bloat.
# Uses PRoot user-space virtualization for Android.

BASE_DIR="$(dirname "$0")"
ROOTFS="$BASE_DIR/rootfs"
PORT="${1:-8080}"

echo "=========================================="
echo " Starting Vastavik CodeOSS (Minimal Ubuntu)"
echo " Server Port: $PORT"
echo "=========================================="

# Environment variables for user-space Ubuntu
export HOME=/root
export USER=root
export TERM=xterm-256color
export LANG=C.UTF-8
export PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

# Launch code-server headless inside PRoot environment
proot \
    --rootfs="$ROOTFS" \
    --bind=/dev \
    --bind=/proc \
    --bind=/sys \
    --bind="$BASE_DIR/workspace:/root/workspace" \
    /usr/bin/code-server \
        --auth none \
        --bind-addr 127.0.0.1:$PORT \
        --disable-telemetry \
        --disable-update-check \
        /root/workspace
