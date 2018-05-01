import pika
import io
from PIL import Image
from PIL import *
from io import BytesIO

connection = pika.BlockingConnection(pika.ConnectionParameters(host='localhost'))
channel = connection.channel()

channel.queue_declare(queue='file_d_attente03', passive=True, durable=True)

print(' [*] Waiting for messages. To exit press CTRL+C')

def callback(ch, method, properties, body):
    print(" [x] Image recue ! %r")
    img = Image.open(body)
    img2 = Image.open(body)

    img = img.resize((1200, 800), Image.ANTIALIAS)
    img2 = img2.resize((800, 400), Image.ANTIALIAS)
    name,garbo = body.split(".")
    name2 = name + "2.jpg"
    name = name + "1.jpg"

    #img.save(buffered, format="PNG")
    try :
        img.save(name, format="JPEG")
    except ValueError:
        print("Erreur de librairie 1")

    try :
        img2.save(name2, format="JPEG")
    except ValueError:
        print("Erreur de librairie 2")


    message = body + " " + name + " " + name2
    channel.basic_publish(exchange='echangeur_topic02',
                          routing_key="log.message",
                          body=message)

    print(" [x] Done")
    ch.basic_ack(delivery_tag = method.delivery_tag)

channel.basic_consume(callback,
                      queue='file_d_attente03',
                      no_ack=False)

channel.start_consuming()
