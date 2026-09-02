import unittest
import json
from ApacheLogConverter import ApacheLogConverter

class TestApacheLogConverter(unittest.TestCase):

    def setUp(self):
        """Instancia o conversor antes de cada teste."""
        """Instantiates the converter before each test."""
        self.converter = ApacheLogConverter()

        # Exemplos de linhas de log válidas (Legítima e Ataque SQLi)
        # Examples of valid log lines (Legitimate and SQLi attack)
        self.valid_log_1 = (
            '192.168.1.45 - - [02/Sep/2026:14:32:10 -0300] '
            '"GET /index.html HTTP/1.1" 200 4523 '
            '"https://www.google.com" "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"'
        )
        self.valid_log_sqli = (
            '185.220.101.5 - - [02/Sep/2026:14:32:15 -0300] '
            '"GET /admin/users.php?id=1%27%20OR%20%271%27=%271 HTTP/1.1" 400 1250 '
            '"-" "python-requests/2.31.0"'
        )
        self.invalid_log = "Linha de log malformatada ou texto aleatorio"

    # --- TESTES PARA convertLine --- TESTS FOR convertLine

    def test_convertLine_valid_log_returns_json_string(self):
        """Garante que uma linha válida retorna uma string JSON e contêm as chaves corretas."""
        """Ensures that a valid line returns a JSON string and contains the correct keys."""
        result = self.converter.convertLine(self.valid_log_1)
        
        self.assertIsNot(result, False)
        self.assertIsInstance(result, str)
        
        # Deserializa para verificar o conteúdo interno
        # Deserializes to verify the internal content.
        data = json.loads(result)
        self.assertEqual(data["ip"], "192.168.1.45")
        self.assertEqual(data["method"], "GET")
        self.assertEqual(data["status"], 200)
        self.assertEqual(data["size"], 4523)

    def test_convertLine_threat_payload_parsing(self):
        """Verifica se payloads de ataque (SQLi) na URL são mantidos sem perda de caracteres."""
        """Verifies whether attack payloads (SQLi) in the URL are preserved without character loss."""
        result = self.converter.convertLine(self.valid_log_sqli)
        data = json.loads(result)
        
        self.assertEqual(data["ip"], "185.220.101.5")
        self.assertIn("OR%20%271%27=%271", data["url"])
        self.assertEqual(data["user_agent"], "python-requests/2.31.0")

    def test_convertLine_invalid_log_returns_false(self):
        """Garante que uma linha fora do padrão retorne False."""
        """Ensures that a non-standard line returns False."""
        result = self.converter.convertLine(self.invalid_log)
        self.assertFalse(result)

    def test_convertLine_empty_string_returns_false(self):
        """Garante que linhas vazias retornem False."""
        """Ensures that empty lines return False."""
        result = self.converter.convertLine("")
        self.assertFalse(result)

    # --- TESTES PARA convertAll --- TESTS FOR convertAll

    def test_convertAll_with_mixed_logs(self):
        """Testa o processamento de uma lista contendo linhas válidas e inválidas."""
        """Tests the processing of a list containing valid and invalid lines."""
        logs = [self.valid_log_1, self.invalid_log, self.valid_log_sqli]
        result = self.converter.convertAll(logs)

        self.assertIsInstance(result, list)
        self.assertEqual(len(result), 2)  # A linha inválida deve ser ignorada - Invalid line must be ignored

    def test_convertAll_only_invalid_logs_returns_false(self):
        """Testa se o retorno é False quando nenhuma linha pode ser convertida."""
        """Tests whether the return value is False when no rows can be converted."""
        logs = [self.invalid_log, "Outra linha invalida"]
        result = self.converter.convertAll(logs)

        self.assertFalse(result)

    def test_convertAll_empty_list_returns_false(self):
        """Testa o comportamento com uma lista totalmente vazia."""
        """Tests the behavior with a completely empty list."""
        result = self.converter.convertAll([])
        self.assertFalse(result)


if __name__ == "__main__":
    unittest.main()