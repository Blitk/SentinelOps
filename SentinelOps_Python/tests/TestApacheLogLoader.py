import unittest
from unittest.mock import patch, mock_open
import sys
from pathlib import Path

sys.path.append(str(Path(__file__).resolve().parent.parent))

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
    def test_load_log_reads_all_new_lines(self, mock_exists, mock_getsize):
        """Testa se loadLog carrega todas as linhas do ficheiro e não deixa passar nenhuma."""
        mock_exists.return_value = True
        mock_getsize.return_value = 500

        # Simula um log com 7 linhas (o antigo limitaria a 5)
        log_data = "linha1\nlinha2\nlinha3\nlinha4\nlinha5\nlinha6\nlinha7\n"

        with patch('builtins.open', mock_open(read_data=log_data)):
            loader = ApacheLogLoader("exemplo.log")
            loader.loadLog()

        # Deve conter TODAS as 7 linhas para não perder dados importantes
        self.assertEqual(len(loader.logContent), 7)
        self.assertEqual(loader.logContent[0], "linha1\n")
        self.assertEqual(loader.logContent[-1], "linha7\n")
        self.assertEqual(loader.lastLogLineLoaded, "linha7\n")

    @patch('os.path.getsize')
    @patch('os.path.exists')
    def test_has_changed_true(self, mock_exists, mock_getsize):
        """Testa se hasChanged detecta quando o ficheiro aumenta de tamanho (novos logs)."""
        mock_exists.return_value = True
        
        loader = ApacheLogLoader("exemplo.log")
        loader.lastPointer = 500  # Simula que já lemos até o byte 500

        # Simula que o tamanho do ficheiro cresceu para 1000 bytes no disco
        mock_getsize.return_value = 1000
        
        self.assertTrue(loader.hasChanged())

    @patch('os.path.getsize')
    @patch('os.path.exists')
    def test_has_changed_false(self, mock_exists, mock_getsize):
        """Testa se hasChanged retorna False se o ficheiro não cresceu."""
        mock_exists.return_value = True
        
        loader = ApacheLogLoader("exemplo.log")
        loader.lastPointer = 500  # Parou no byte 500

        # Simula que o ficheiro continua exatamente com 500 bytes no disco
        mock_getsize.return_value = 500
        
        self.assertFalse(loader.hasChanged())


if __name__ == "__main__":
    unittest.main()
