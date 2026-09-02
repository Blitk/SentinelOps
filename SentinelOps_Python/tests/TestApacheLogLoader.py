import unittest
from unittest.mock import patch, mock_open
from ApacheLogLoader import ApacheLogLoader


class TestApacheLogLoader(unittest.TestCase):

    @patch('os.path.exists')
    def test_file_exists(self, mock_exists):
        """Testa se o método fileExists retorna True quando o arquivo existe."""
        mock_exists.return_value = True
        loader = ApacheLogLoader("exemplo.log")
        self.assertTrue(loader.fileExists())

    @patch('os.path.exists')
    def test_file_does_not_exist(self, mock_exists):
        """Testa se o método fileExists retorna False quando o arquivo não existe."""
        mock_exists.return_value = False
        loader = ApacheLogLoader("exemplo.log")
        self.assertFalse(loader.fileExists())

    @patch('os.path.getsize')
    @patch('os.path.exists')
    def test_is_empty_true(self, mock_exists, mock_getsize):
        """Testa se isEmpty retorna True para arquivos vazios."""
        mock_exists.return_value = True
        mock_getsize.return_value = 0
        loader = ApacheLogLoader("exemplo.log")
        self.assertTrue(loader.isEmpty())

    @patch('os.path.getsize')
    @patch('os.path.exists')
    def test_is_empty_false(self, mock_exists, mock_getsize):
        """Testa se isEmpty retorna False para arquivos com conteúdo."""
        mock_exists.return_value = True
        mock_getsize.return_value = 1024
        loader = ApacheLogLoader("exemplo.log")
        self.assertFalse(loader.isEmpty())

    @patch('os.path.getsize')
    @patch('os.path.exists')
    def test_load_log_reads_last_five_lines(self, mock_exists, mock_getsize):
        """Testa se loadLog carrega no máximo as últimas 5 linhas do arquivo."""
        mock_exists.return_value = True
        mock_getsize.return_value = 500

        ### Simula um log com 7 linhas

        log_data = "linha1\nlinha2\nlinha3\nlinha4\nlinha5\nlinha6\nlinha7\n"

        with patch('builtins.open', mock_open(read_data=log_data)):
            loader = ApacheLogLoader("exemplo.log")
            loader.loadLog()

        # Deve conter apenas as últimas 5 linhas
        self.assertEqual(len(loader.logContent), 5)
        self.assertEqual(loader.logContent[0], "linha3\n")
        self.assertEqual(loader.logContent[-1], "linha7\n")
        self.assertEqual(loader.lastLogLineLoaded, "linha7\n")


    @patch('os.path.exists')
    def test_has_changed_true(self, mock_exists):
        """Testa se hasChanged detecta quando novas linhas são adicionadas."""
        mock_exists.return_value = True
        loader = ApacheLogLoader("exemplo.log")
        loader.lastLogLineLoaded = "linha7\n"

        # Simula que a última linha agora mudou para 'linha8'
        with patch('builtins.open', mock_open(read_data="linha8\n")):
            self.assertTrue(loader.hasChanged())

    @patch('os.path.exists')
    def test_has_changed_false(self, mock_exists):
        """Testa se hasChanged retorna False se a última linha continuar igual."""
        mock_exists.return_value = True
        loader = ApacheLogLoader("exemplo.log")
        loader.lastLogLineLoaded = "linha7\n"
        with patch('builtins.open', mock_open(read_data="linha7\n")):
            self.assertFalse(loader.hasChanged())


if __name__ == "__main__":
    unittest.main()