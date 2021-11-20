# BluetoothClient
Adroid приложение, отображающее картинки и воспроизводящее звук при получении сигнала по bluetooth. 

- ViewBinding
- MVP
- Moxy
- Cicerone
- Dagger
- RxJava3

Это приложение - дополнение к основной программе, работающей с энцефалографом. Его работа заключается в отображении картинок определенной тематики и воспроизведении звука в момент получения сигнала от сервера.
(Bluetooth сервер для отладки https://github.com/LenaTopoleva/BluetoothServer)

#### Что принимает приложение

Сервер отправляет сообщения типа String, содержащие json с информацией о типе команды, подтипе, имени файла для отображения и необходимости воспроизведения стандартного звукового сигнала, которые мапятся в класс:
```
data class BluetoothResponse (val type: String, val subtype: String, val fileName: String, val tone: Boolean)
```

#### Перед началом работы

Для быстроты отображения, картинки (и звуковой файл) не передаются по bluetooth, а сохраняются заранее в памяти телефона в отдельной папке. 

В эту папке необходимо добавить файл конфигурации "configuration.txt" типа:
```
actions-picturesaction
objects-picturesobject
other-picturesother
sounds-sounds
tone-tone.wav
```
Где для каждого типа картинок через дефис указывается имя папки, в которой они хранятся, в последней строке - имя стандартного звукового файла для "отбивки".

#### Что делать дальше

При первом старте приложение попросит выбрать директорию с медиа файлами в установленном DocumentsProvider-е и сохранит ее в SharedPreferences. 
Изменить ее можно в любой момент, нажав на правую иконку в меню приложения.

Для подключения к серверу, надо нажать кнопку bluetooth в меню, которая открывает список всех доступных устройств. При успешном подключении, MAC-адрес сервера также сохраняется в SP.

<img src="/../readme/app/src/main/res/readme/bc_main.jpg" width="300" height="600">
<img src="/../readme/app/src/main/res/readme/bc_bluetooth_list.jpg" width="300" height="600">
<img src="/../readme/app/src/main/res/readme/bc_pick_dir.jpg" width="300" height="600">





