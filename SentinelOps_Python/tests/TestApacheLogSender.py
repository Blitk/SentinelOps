import unittest
from unittest.mock import patch, MagicMock
import requests
import sys
from pathlib import Path

sys.path.append(str(Path(__file__).resolve().parent.parent))

from ApacheLogSender import ApacheLogSender


class TestApacheLogSender(unittest.TestCase):

    def setUp(self):
        """
           Executado antes de cada teste para instanciar o objeto padrão.
           Executed before each test to instantiate the default object
        """
        self.sender = ApacheLogSender()

    def test_send_sem_conteudo(self):
        """
            Garante que retorna erro ao tentar enviar conteúdo vazio.
            Ensures that an error is returned when attempting to send empty content.
        """
        self.sender.setContent([])
        sucesso, mensagem = self.sender.send()
        
        self.assertFalse(sucesso)
        self.assertEqual(mensagem, "None Content to Send")

    @patch('requests.post')
    def test_send_sucesso(self, mock_post):
        """
            Garante o comportamento correto quando a API responde com sucesso (200 OK).
            Ensures correct behavior when the API responds successfully (200 OK).
        """
        # Configura o mock para simular uma resposta de sucesso -- Configures the mock to simulate a successful response.
        mock_response = MagicMock()
        mock_response.json.return_value = {"status": "success"}
        mock_post.return_value = mock_response
        
        conteudo_teste = [{"log": "teste de log"}]
        self.sender.setContent(conteudo_teste)

        sucesso, resposta = self.sender.send()
        
        # Validações - Validations
        self.assertTrue(sucesso)
        self.assertEqual(resposta, {"status": "success"})
        self.assertEqual(len(self.sender.content), 0) # Garante que a lista foi limpa - Grant to clear the list
        self.assertEqual(mock_post.call_count, 1)
        url_chamada = mock_post.call_args[0][0]
        self.assertEqual(url_chamada, "http://127.0.0.1:8080/")
        
    @patch('requests.post')
    def test_send_erro_timeout(self, mock_post):
        """
            Valida o tratamento do erro de Timeout.
            Validates the handling of the timeout error.
        """
        # Configura o mock para disparar a exceção de Timeout -- Configures the mock to throw a Timeout exception.
        mock_post.side_effect = requests.exceptions.Timeout()
        
        self.sender.setContent([{"log": "teste"}])
        sucesso, mensagem = self.sender.send()
        
        self.assertFalse(sucesso)
        self.assertEqual(mensagem, "Timeout exceeded")

    @patch('requests.post')
    def test_send_erro_http(self, mock_post):
        """
            Valida o tratamento de erros HTTP (ex: 500, 404).
            Validates HTTP error handling (e.g., 500, 404).
        """
        # Configura o mock para disparar uma falha de HTTPError -- Configures the mock to raise an HTTPError failure.
        mock_post.side_effect = requests.exceptions.HTTPError("Internal Server Error")
        
        self.sender.setContent([{"log": "teste"}])
        sucesso, mensagem = self.sender.send()
        
        self.assertFalse(sucesso)
        self.assertIn("Server error:", mensagem)

    @patch('requests.post')
    def test_send_erro_conexao_geral(self, mock_post):
        """
            Valida falhas gerais na rede ou conexão.
            Validates general network or connection failures.
        """
        # Configura o mock para disparar erro genérico do requests -- Configures the mock to raise a generic requests error.
        mock_post.side_effect = requests.exceptions.RequestException("Connection refused")
        
        self.sender.setContent([{"log": "teste"}])
        sucesso, mensagem = self.sender.send()
        
        self.assertFalse(sucesso)
        self.assertIn("General conection error:", mensagem)

if __name__ == '__main__':
    unittest.main()
