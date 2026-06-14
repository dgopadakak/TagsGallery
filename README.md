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