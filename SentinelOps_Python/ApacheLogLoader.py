import os

"""
Esta classe recebe o caminho absoluto do log e carrega apenas
as novas linhas desde a última leitura.

This class receives the absolute path to the log and loads only
the new lines since the last read.
"""

class ApacheLogLoader:

    def __init__(self, logPath):

        self.logPath = logPath
        self.logContent = []
        self.lastLogLineLoaded = ""
        self.lastPosition = 0

    # Testa a existência do arquivo
    # Checks if the file exists
    def fileExists(self):

        return os.path.exists(self.logPath)

    # Testa se o arquivo é vazio
    # Checks if the file is empty
    def isEmpty(self):

        if not self.fileExists():
            return True

        return os.path.getsize(self.logPath) == 0

    # Verifica se existem novos logs
    # Checks if there are new logs
    def hasChanged(self):

        if not self.fileExists():
            return False

        current_size = os.path.getsize(self.logPath)

        # Caso o arquivo tenha sido truncado/rotacionado
        if current_size < self.lastPosition:
            self.lastPosition = 0

        return current_size > self.lastPosition

    # Carrega todos os novos logs desde a última leitura
    # Loads all new logs since the last read
    def loadLog(self):

        if not self.fileExists():
            return False

        if self.isEmpty():
            return False

        # Detecta truncamento/rotação do arquivo
        current_size = os.path.getsize(self.logPath)

        if current_size < self.lastPosition:
            self.lastPosition = 0

        self.logContent.clear()

        with open(self.logPath, "r", encoding="utf-8") as log:

            # Volta para a última posição lida
            log.seek(self.lastPosition)

            # Lê somente as novas linhas
            for line in log:

                line = line.rstrip("\n")

                if line:
                    self.logContent.append(line)

            # Salva a nova posição
            self.lastPosition = log.tell()

        if self.logContent:

            self.lastLogLineLoaded = self.logContent[-1]

            return True

        return False
