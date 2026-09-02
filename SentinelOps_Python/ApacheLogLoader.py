import os
from collections import deque

"""
Esta classe recebe o caminho absoluto do log e o carrega

	- Para pegar no Linux, use dentro do diretório do arquivo: pwd
	- Para pegar no Windows, use dentro do diretório do arquivo: echo %cd%
	- Caminho padrão no Ubuntu: /var/log/apache2/access.log

self class receives the absolute path to the log and loads it

	- To get it on Linux, use the following command within the file's directory: pwd
	- To get it on Windows, use the following command within the file's directory: echo %cd%
	- Default path on Ubuntu: /var/log/apache2/access.log
"""

class ApacheLogLoader:

	def __init__(self, logPath):

		self.logPath = logPath
		self.logContent = list()
		self.lastLogLineLoaded = ""


	# Testa a existência do arquivo
	# Checks if the file exists
	def fileExists(self):

		return os.path.exists(self.logPath)


	# Testa se o arquivo é nulo/vazio
	# Checks if the file is empty
	def isEmpty(self):

		if os.path.getsize(self.logPath) > 0:
			return False
		else:
			return True


	# Carrega as ultimas 5 linhas do log
	# Load the last 5 lines
	def loadLog(self):

		if self.fileExists() and not self.isEmpty():
			self.logContent.clear()
			with open(self.logPath, "r", encoding="utf-8") as log:

				lastFive = deque(log, maxlen=5)
				for line in lastFive:
					self.logContent.append(line)

				self.lastLogLineLoaded = self.logContent[ len(self.logContent) - 1 ]


	# Verifica se mudou desde o ultimo carregamento
	# Verify if changes since last load
	def hasChanged(self):
		if self.lastLogLineLoaded != "":
			with open(self.logPath, "r", encoding="utf-8") as log:

				last = deque(log, maxlen=1)

				# Se o deque não estiver vazio, extrai a string de dentro dele
				last_line = last[0] if last else ""

				if last_line != self.lastLogLineLoaded:
					return True
				else:
					return False
		else:
			return False