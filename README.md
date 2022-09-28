# project Share It

Сервис для шеринга (от англ. share — «делиться») вещей.
Шеринг как экономика совместного использования набирает сейчас всё большую полярность.

Проект обмена вещами, возможно далеко похожее на Avito.

# History:
### 2022-09-28
- вынес DTO в отдельный модуль
- отсортировал тесты по модулям

### 2022-09-27
- Docker-compose
- gateway images
- server images
- postgres images

### 2022-09-25
- Разбить приложение ShareIt на два — shareIt-server и shareIt-gateway. Они будут общаться друг с другом через REST. 
- Вынести в shareIt-gateway всю логику валидации входных данных — кроме той, которая требует работы с БД.

## Разбиваю монолит на микросервисы
- Вся работа в этом спринте будет вестись в ветке **_add-docker_**.

### 2022-09-19
Небольшой рефакторинг, оптимизация кода.
### 2022-09-18
- Тесты для Сервисов
- Тесты для Мапперов
- Тесты для Контроллеров
### 2022-09-17
- тесты для репозирориев
- тесты на DTO JsonTest
-
## Добавляем тесты

### 2022-09-11
## Добавляем пагинацию к существующим эндпоинтам

## Добавляем опцию ответа на запрос

### 2022-09-10
## Добавляем запрос вещи
- В этом спринте разработка будет вестись в ветке add-item-requests

## Нужно добавить четыре новых эндпоинта:
- новый запрос вещи.
- получить список своих запросов вместе с данными об ответах на них
- получить список запросов,
- получить данные об одном конкретном запросе вместе с данными об ответах на него.

## Техническое задание. Часть 3
- добавить возможность создавать запрос вещи и добавлять вещи в ответ на запросы других пользователей.


### 2022-09-02
## Добавление отзывов
- пользователи могут оставлять отзывы на вещь после того, как взяли её в аренду.
- JPARepository для Comment

### 2022-09-01
## Добавление дат бронирования при просмотре вещей
- Теперь нужно, чтобы владелец видел даты последнего и ближайшего следующего бронирования для каждой вещи, когда просматривает список (GET /items).

## Реализация функции бронирования
- JpaRepository для Booking

### 2022-08-29
- JpaRepository для Users
- JpaRepository для Items

### 2022-08-28
## Настройка JPA
- проставил аннотации JPA для моделей

### 2022-08-27
## Создание базы данных

- Напишисал SQL-код для создания всех таблиц и сохраните его в файле resources/schema.sql

## Init
В этом спринте разработка будет вестись в ветке add-bookings
- добавил зависимость spring-boot-starter-data-jpa и драйвер postgresql в файл pom.xml.

#### 2022-08-15
### Разработка контроллеров

- ItemController
#### 2022-08-14
- UserController

### Создание DTO-объектов и мапперов

- User модель
- Item модель

### Реализация модели данных

Создание структуры данных

- User
- Item
- Booking
- Requests

### Создайте отдельную ветку

У вас уже готов шаблон проекта с использованием Spring Boot. Создайте ветку **add-controllers** и
переключитесь на неё — в этой ветке будет вестись вся разработка для первого спринта.

### Sprint 1

# java-shareit

Template repository for Shareit project.
