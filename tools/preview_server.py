#!/usr/bin/env python3
"""Tiny static server for local previews: `python3 tools/preview_server.py 8080`."""
import http.server
import socketserver
import sys
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PORT = int(sys.argv[1]) if len(sys.argv) > 1 else 8080


class Handler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=ROOT, **kwargs)

    def do_GET(self):
        if self.path in ('', '/'):
            self.path = '/frontend/index.html'
        return super().do_GET()

    def do_HEAD(self):
        if self.path in ('', '/'):
            self.path = '/frontend/index.html'
        return super().do_HEAD()

    def guess_type(self, path):
        if path.lower().endswith('.apk'):
            return 'application/vnd.android.package-archive'
        if path.lower().endswith('.zip'):
            return 'application/zip'
        return super().guess_type(path)

    def end_headers(self):
        self.send_header('Cache-Control', 'no-store')
        super().end_headers()

    def log_message(self, fmt, *args):
        pass


socketserver.TCPServer.allow_reuse_address = True
with socketserver.TCPServer(('0.0.0.0', PORT), Handler) as httpd:
    httpd.serve_forever()
