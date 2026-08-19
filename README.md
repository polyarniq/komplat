# KomPlat - Учёт коммунальных платежей

Android-приложение для ведения учёта коммунальных расходов.

## Возможности

- Учёт расходов по ресурсоснабжающим организациям
- Сравнение расходов за разные периоды
- Добавление, редактирование и удаление статей расходов
- Прикрепление чеков и квитанций к карточкам компаний
- Экспорт данных в CSV
- Графики расходов по месяцам

## Стек технологий

- **Язык**: Kotlin
- **UI**: Jetpack Compose, Material 3
- **Архитектура**: MVVM + Clean Architecture
- **БД**: SQLite (без Room)
- **DI**: Hilt
- **Навигация**: Navigation Compose

## Структура проекта

```
app/src/main/java/ru/komplat/
├── data/                    # Слой данных
│   ├── local/db/           # SQLite база данных
│   └── repository/         # Реализации репозиториев
├── domain/                  # Доменный слой
│   ├── model/              # Модели данных
│   ├── repository/         # Интерфейсы репозиториев
│   └── usecase/            # Бизнес-логика
├── presentation/            # Слой представления
│   ├── navigation/         # Навигация
│   ├── screens/            # Экраны
│   ├── components/         # Переиспользуемые компоненты
│   └── theme/              # Тема приложения
└── di/                      # Dependency Injection
```

## Сборка

```bash
# Установите переменные окружения
export ANDROID_HOME="/path/to/android-sdk"
export JAVA_HOME="/path/to/jdk-21"

# Соберите debug APK
./gradlew assembleDebug

# APK будет в app/build/outputs/apk/debug/
```

## Требования

- Android SDK 34
- JDK 21
- Gradle 8.5

## Лицензия

MIT License
