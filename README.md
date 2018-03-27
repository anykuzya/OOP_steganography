# OOP_steganography

# Общие сведения(техническое задание)

Консольная утилита, предоставляющая функционал по встраиванию произвольного текста внутрь изображения 
в формате Microsoft BMP без визуально заметного изменения картинки(или с малозаметными человеческому 
глазу изменениями). Консольный интерфейс позволяет вводить шифруемый текст из файла. Поддержано 
несколько различных кодировок.


Данная консольная утилита предполагает 2 режима работы(режим указывается в аргументе командной строки), 
а именно: кодирование(встраивание текста в картинку) и декодирование(извлечение из картинки текста). 
При этом, вся информация, необходимая для декодирования(кодировка, размер текста и т.д.) 
определяется автоматически(т.е. при кодировании помимо текста в картинку встраивается дополнительная мета-информация). 
Также реализована кастомизация степени изменения картинки в результате встраивания в неё текста(сколько бит 
в каждом канале изображения отводится под хранение информации). 
При невозможности уместить весь текст в картинку текст обрезается. При этом, если в тексте содержатся
юникод символы, которые кодируются несколькими байтами, они будут обрезаны корректно, т.е. в итоге будет 
закодировано целое число символов.

Детали реализованного функционала:
* поддержан формат BMP версии CORE, 3, 4 или 5 с режимами кодирования RLE и pixmap произвольной допустимой битности 
и количества каналов.
* поддерживаемые кодировки: US-ASCII, UTF-8, UTF-16
* степень изменения картинки указывается в количестве бит на канал изображения. 
Поддерживаемые значения: 1, 2, 4 или 8

## Установка и использование

Для сборки и использования необходимы:
* java версии 8
* gradle версии 4

Сборка проекта с прогоном тестов:
```bash
gradle clean build distTar
tar xf build/distributions/bmp-steganography-1.0-SNAPSHOT.tar
```

Запуск:
```bash
cd bmp-steganography-1.0-SNAPSHOT/bin/
 ./bmp-steganography
```