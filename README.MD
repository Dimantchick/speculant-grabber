Проект переехал в общий https://github.com/Dimantchick/hobot

# Торговый инструментарий для Тинькофф инвестиций.

Модуль граббера исторических свечей.

## Зависимости

Установить в локальный репозиторий [Core проект](https://github.com/Dimantchick/speculant-core).

## Начало работы

Создать config.properties и application.properties (использовать .default файлы как основу).

Сделать необходимые настройки.

Запустить проект и перейти по  [http://localhost:8080/instruments/grab](http://localhost:8080/instruments/grab).

ОБЯЗАТЕЛЬНО дождаться загрузки страницы.

При повторном обращении к странице будут удалены все инструменты и заново сформированы от брокера.

## Использование

[http://localhost:8080/instruments](http://localhost:8080/instruments)
Отображает список всех инструментов.

Инструменты в состоянии DISABLED(по-умолчанию) граббер игнорирует.

У инструментов в состоянии READY и выше опрашиваются раз в час часовые свечи.

У инструментов в состоянии выше READY постоянно опрашиваются исторические 5мин свечи.

## Зачем все это

Этот модуль предназначен для работы в связке с еще недоделанными модулями торговых инструментов.
Сейчас можно использовать либо в учебных целях, либо для написания собственных инструментов.

## Специалистам буду благодарен за код-ревью
Почта для связи Dimantchick@gmail.com
