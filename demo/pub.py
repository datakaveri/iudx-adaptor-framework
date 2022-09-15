import pika
import json


data1 = {"index": 4, "deviceId":"abc-123","k1":805,"observationDateTime":"2022-09-17T11:27:04.688Z"}
data2 = {"index": 5, "deviceId":"yyy-123","k1":801,"observationDateTime":"2022-09-17T11:29:04.688Z"}
data3 = {"index": 6, "deviceId":"xxx-123","k1":801,"observationDateTime":"2022-09-17T11:29:04.688Z"}
data4 = {"index": 7, "deviceId":"yyy-123","k1":801,"observationDateTime":"2022-09-17T11:39:04.688Z"}

parameters = pika.URLParameters('amqp:guest:guest@localhost:5672/%2F')
connection = pika.BlockingConnection(parameters)
channel = connection.channel()

channel.basic_publish('adaptor-test', 'test', json.dumps(data1),
                        pika.BasicProperties(content_type='text/plain', delivery_mode=1))
channel.basic_publish('adaptor-test', 'test', json.dumps(data2),
                        pika.BasicProperties(content_type='text/plain', delivery_mode=1))
channel.basic_publish('adaptor-test', 'test', json.dumps(data3),
                        pika.BasicProperties(content_type='text/plain', delivery_mode=1))
channel.basic_publish('adaptor-test', 'test', json.dumps(data4),
                        pika.BasicProperties(content_type='text/plain', delivery_mode=1))

connection.close()
