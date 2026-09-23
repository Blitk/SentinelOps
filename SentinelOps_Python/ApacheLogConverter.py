from datetime import datetime
import re


class ApacheLogConverter:

    def __init__(self):
        self.pattern = re.compile(
            r'(?P<sourceip>\S+)\s+\S+\s+\S+\s+\['
            r'(?P<datetime>[^\]]+)\]\s+"'
            r'(?P<method>\S+)\s+(?P<url>\S+)\s+(?P<protocol>[^"]+)"\s+'
            r'(?P<statuscode>\d+)\s+'
            r'(?P<size>\d+)\s+"'
            r'(?P<referrer>[^"]*)"\s+"'
            r'(?P<user_agent>[^"]*)"'
        )

    def convertLine(self, content):
        match = self.pattern.match(content)

        if not match:
            return False

        log_dict = match.groupdict()

        datetime_value = log_dict["datetime"].strip()

        timestamp = datetime.strptime(
            datetime_value,
            "%d/%b/%Y:%H:%M:%S"
        ).astimezone().isoformat()

        return {
            "timestamp": timestamp,
            "sourceip": log_dict["sourceip"],
            "method": log_dict["method"],
            "path": log_dict["url"],
            "statuscode": int(log_dict["statuscode"]),
            "source": "APACHE"
        }

    def convertAll(self, content):
        data = []

        for line in content:
            js = self.convertLine(line)

            if js is False:
                continue

            data.append(js)

        if len(data) == 0:
            return False

        return data