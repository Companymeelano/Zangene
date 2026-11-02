import base64
import importlib.util
import json
import os
import threading
import unittest
from http.client import HTTPConnection
from pathlib import Path
from unittest.mock import patch

path = Path(__file__).resolve().parents[1] / 'backend/ai_proxy/server.py'
spec = importlib.util.spec_from_file_location('studio_proxy', path)
proxy = importlib.util.module_from_spec(spec)
spec.loader.exec_module(proxy)

class ProxyTests(unittest.TestCase):
    def setUp(self):
        self.valid = {'name': 'چای سیاه', 'brand': 'ساقدوش',
                      'image_jpeg_base64': base64.b64encode(b'\xff\xd8\xffsample\xff\xd9').decode()}
        self.answer = {'nameFa': 'چای سیاه', 'category': 'چای', 'description': 'چای سیاه ساقدوش'}

    def test_provider_payloads_and_credentials_are_headers(self):
        calls = []
        def fake(url, headers, body):
            calls.append((url, headers, body))
            if 'googleapis' in url:
                return {'candidates': [{'content': {'parts': [{'text': json.dumps(self.answer)}]}}]}
            return {'choices': [{'message': {'content': json.dumps(self.answer)}}]}
        env = {'GEMINI_API_KEY': 'test-gemini', 'OPENAI_API_KEY': 'test-openai', 'GROQ_API_KEY': 'test-groq'}
        with patch.object(proxy, 'call_json', side_effect=fake):
            for provider in ('gemini', 'openai', 'groq'):
                result = proxy.normalize_result(proxy.provider_request(provider, 'چای سیاه', 'ساقدوش', self.valid['image_jpeg_base64'], env))
                self.assertEqual(result['category'], 'چای')
        self.assertEqual(len(calls), 3)
        for url, headers, body in calls:
            self.assertNotIn('test-', url)
            self.assertTrue('Authorization' in headers or 'x-goog-api-key' in headers)
            self.assertIn(self.valid['image_jpeg_base64'], json.dumps(body))

    def test_failover_and_untrusted_response(self):
        env = {'GEMINI_API_KEY': 'test', 'OPENAI_API_KEY': 'test', 'GROQ_API_KEY': 'test'}
        def fake(provider, *args):
            if provider == 'gemini': raise OSError('offline')
            if provider == 'openai': return '{}'
            return json.dumps(self.answer)
        with patch.object(proxy, 'provider_request', side_effect=fake):
            result = proxy.generate(self.valid, env)
        self.assertEqual(result['_provider'], 'groq')
        self.assertEqual(result['nameFa'], 'چای سیاه')
        self.assertEqual(result['ingredients'], '')
        with self.assertRaises(ValueError): proxy.normalize_result('{"nameFa":"x","category":"y","description":42}')
        with self.assertRaises(ValueError): proxy.validate_request({'name':'tea','image_jpeg_base64':'bad!'})

    def test_http_auth_and_validation(self):
        token = 'a' * 40
        with patch.dict(os.environ, {'STUDIO_ACCESS_TOKEN': token}, clear=False):
            server = proxy.Server(('127.0.0.1', 0))
            thread = threading.Thread(target=server.serve_forever, daemon=True); thread.start()
            try:
                def post(auth, body):
                    conn = HTTPConnection('127.0.0.1', server.server_port, timeout=5)
                    conn.request('POST', '/v1/product-studio', json.dumps(body), {'Authorization': auth, 'Content-Type': 'application/json'})
                    r = conn.getresponse(); data = json.loads(r.read()); conn.close(); return r.status, data
                self.assertEqual(post('', self.valid)[0], 401)
                self.assertEqual(post('Bearer ' + token, {'name':'x'})[0], 400)
                with patch.object(proxy, 'generate', return_value=self.answer):
                    status, result = post('Bearer ' + token, self.valid)
                    self.assertEqual(status, 200); self.assertEqual(result['category'], 'چای')
            finally:
                server.shutdown(); server.server_close(); thread.join()

if __name__ == '__main__': unittest.main()
