# TagsGallery

TagsGallery - это Android-приложение для каталогизации медиа с поддержкой тегов.

## Возможности

- 🏷️ Создание, изменение и удаление тегов
- 🖼️ Гибкое присвоение тегов различным видам медиа-файлов
- 🗑️ Удаление медиа из приложения (очистка всех связей с тегами)
- 🔍 Поиск по тегам
- 🎨 UI на Jetpack Compose
- 🌙 Поддержка тем (светлая/темная)
- 💾 Локальное хранение данных

## Структура проекта

Проект разделен на несколько модулей:

- [app](app/README.md) - Основной модуль приложения
- [tags](tags/README.md) - Feature-модуль экрана для работы с тегами
- [add](add/README.md) - Feature-модуль экрана для добавления медиа с присвоением тегов
- [gallery](gallery/README.md) - Feature-модуль экрана просмотра и поиска по тегам среди добавленных медиа
- [core/local_storage](core/local_storage/README.md) - Core-модуль для взаимодействия с локальным хранилищем
- [core/compose](core/compose/README.md) - Core-модуль общих UI компонентов

## Технологии

- Kotlin
- Jetpack Compose
- PhotoPicker
- Coil
- Media 3
- MVVM архитектура
- Room, DataStore для локального хранения
- Hilt для DI
- Coroutines, Flow для асинхронных операций
- Material Design 3

## Политика конфиденциальности

**Приложение:** TagsGallery (`com.dgopadakak.tagsgallery`)  
**Разработчик:** dgopadakak  
**Дата вступления в силу:** 28 августа 2026 г.  
**Последнее обновление:** 28 августа 2026 г.

### Кратко

TagsGallery не собирает, не передаёт и не хранит за пределами вашего устройства никаких персональных данных. У приложения нет серверов, аккаунтов, аналитики и рекламы. Всё, что вы создаёте в приложении, остаётся на устройстве.

### Какие данные обрабатываются

Приложение сохраняет во внутреннем хранилище устройства:

- теги, которые вы создаёте: название, цвет, время последнего изменения;
- связи между тегами и выбранными вами медиафайлами — в виде системных идентификаторов (ссылок) этих файлов;
- служебные настройки интерфейса: отметки о том, какие подсказки уже были показаны.

Приложение **не копирует** сами фотографии и видео: оно хранит только ссылки на них и показывает их из галереи устройства. Приложение не собирает имя, адрес электронной почты, номер телефона, контакты, местоположение, идентификаторы устройства и рекламные идентификаторы.

### Доступ к медиафайлам

TagsGallery не запрашивает никаких разрешений Android. Выбор фотографий и видео выполняется через системный компонент Android Photo Picker: приложение получает доступ только к тем файлам, которые вы выбрали сами, и не имеет доступа ко всей галерее.

### Передача данных третьим лицам

Данные не передаются никому. В приложении нет:

- аналитики и статистики использования;
- систем сбора отчётов о сбоях;
- рекламных SDK и трекеров;
- сетевых запросов — у приложения нет даже разрешения `INTERNET`, поэтому оно технически не способно отправить что-либо в сеть.

### Резервное копирование

В приложении включено стандартное резервное копирование Android (Android Auto Backup). Если эта функция включена в настройках вашего устройства и аккаунта Google, операционная система может копировать данные приложения (базу тегов и настройки) в ваш Google Диск. Это механизм Google, а не разработчика: копия принадлежит вам, хранится в вашем аккаунте, и разработчик не имеет к ней доступа. Отключить резервное копирование можно в настройках устройства: «Настройки → Google → Резервное копирование».

### Хранение и удаление данных

Данные хранятся на устройстве до тех пор, пока вы их не удалите. Удалить их можно в любой момент: через интерфейс самого приложения, через «Настройки → Приложения → TagsGallery → Хранилище → Очистить данные» или удалив приложение — при удалении все локальные данные приложения стираются.

### Дети

Приложение не предназначено для детей младше 13 лет и не ориентировано на них специально. Оно не собирает персональные данные ни о ком, включая детей.

### Ваши права

Поскольку приложение не собирает и не передаёт данные, у разработчика нет ваших данных, которые можно было бы предоставить, исправить или удалить по запросу. Все данные полностью находятся под вашим контролем на вашем устройстве, что покрывает права, предусмотренные GDPR, CCPA и аналогичными законами.

### Безопасность

Данные приложения хранятся в его приватном внутреннем хранилище, изолированном средствами Android от других приложений. Дополнительно они защищены штатным шифрованием устройства, если оно включено.

### Изменения политики

Актуальная версия политики всегда находится в этом файле. История всех изменений публично доступна в истории коммитов репозитория. Существенные изменения сопровождаются обновлением даты выше.

### Контакты

Вопросы по конфиденциальности: dgopadakakv4@gmail.com  
Также можно создать обращение: https://github.com/dgopadakak/TagsGallery/issues

Исходный код приложения открыт и доступен для проверки: https://github.com/dgopadakak/TagsGallery

## Privacy Policy

**Application:** TagsGallery (`com.dgopadakak.tagsgallery`)  
**Developer:** dgopadakak  
**Effective date:** August 28, 2026  
**Last updated:** August 28, 2026

### Summary

TagsGallery does not collect, transmit, or store any personal data outside your device. The app has no servers, no accounts, no analytics, and no advertising. Everything you create in the app stays on your device.

### What data is processed

The app stores the following in the device's internal storage:

- the tags you create: name, color, and time of last modification;
- links between tags and the media files you selected, stored as the system identifiers (references) of those files;
- interface preferences: flags recording which hints have already been shown.

The app **does not copy** your photos and videos: it stores only references to them and displays them from the device gallery. The app does not collect your name, email address, phone number, contacts, location, device identifiers, or advertising identifiers.

### Access to media files

TagsGallery requests no Android permissions. Photos and videos are selected through the system Android Photo Picker: the app receives access only to the files you pick yourself and has no access to your gallery as a whole.

### Sharing with third parties

No data is shared with anyone. The app contains no:

- usage analytics or statistics;
- crash reporting;
- advertising SDKs or trackers;
- network requests — the app does not even declare the `INTERNET` permission, so it is technically incapable of sending anything over the network.

### Backups

The app has standard Android Auto Backup enabled. If this feature is turned on in your device and Google account settings, the operating system may copy the app's data (the tag database and preferences) to your Google Drive. This is a Google mechanism, not the developer's: the backup belongs to you, is stored in your account, and the developer has no access to it. You can turn backups off in your device settings: "Settings → Google → Backup".

### Data retention and deletion

Data is stored on your device until you delete it. You can delete it at any time: from within the app itself, via "Settings → Apps → TagsGallery → Storage → Clear data", or by uninstalling the app — uninstalling erases all of the app's local data.

### Children

The app is not intended for or directed at children under 13. It collects no personal data about anyone, children included.

### Your rights

Because the app neither collects nor transmits data, the developer holds no data of yours that could be provided, corrected, or deleted on request. All data remains entirely under your control on your device, which satisfies the rights granted by the GDPR, the CCPA, and comparable laws.

### Security

The app's data is stored in its private internal storage, isolated from other applications by Android. It is additionally protected by the device's built-in encryption when that is enabled.

### Changes to this policy

The current version of the policy is always in this file. The full history of changes is publicly available in the repository's commit history. Material changes are accompanied by an update to the dates above.

### Contact

Privacy questions: dgopadakakv4@gmail.com  
You can also open an issue: https://github.com/dgopadakak/TagsGallery/issues

The app's source code is open and available for inspection: https://github.com/dgopadakak/TagsGallery
