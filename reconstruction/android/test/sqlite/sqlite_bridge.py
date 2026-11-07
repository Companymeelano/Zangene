"""Test-only line bridge to CPython SQLite; no Android runtime is involved."""
import base64
import sqlite3
import sys

conn = sqlite3.connect(':memory:', isolation_level=None)
def dec(value):
    return base64.b64decode(value).decode('utf-8')
for line in sys.stdin:
    try:
        fields = line.rstrip('\n').split('\t')
        command, args = fields[0], [dec(s) for s in fields[1:]]
        if command == 'exec':
            conn.execute(args[0])
            result = ''
        elif command == 'query':
            result = '1' if conn.execute(args[0], args[1:]).fetchone() else '0'
        elif command == 'insert':
            table, *pairs = args
            cols, vals = pairs[::2], pairs[1::2]
            if not table.isidentifier() or any(not c.isidentifier() for c in cols):
                raise ValueError('invalid identifier')
            sql = 'INSERT INTO "' + table + '" (' + ','.join('"'+c+'"' for c in cols) + ') VALUES (' + ','.join('?' for _ in cols) + ')'
            conn.execute(sql, vals)
            result = ''
        elif command == 'update':
            table, where, argc, *rest = args
            argc = int(argc)
            where_args, pairs = rest[:argc], rest[argc:]
            cols, vals = pairs[::2], pairs[1::2]
            if not table.isidentifier() or any(not c.isidentifier() for c in cols):
                raise ValueError('invalid identifier')
            sql = 'UPDATE "' + table + '" SET ' + ','.join('"'+c+'"=?' for c in cols) + ' WHERE ' + where
            result = str(conn.execute(sql, vals + where_args).rowcount)
        else:
            raise ValueError('unknown command')
        print('OK\t' + base64.b64encode(result.encode()).decode(), flush=True)
    except Exception as exc:
        print('ERR\t' + base64.b64encode(str(exc).encode()).decode(), flush=True)
