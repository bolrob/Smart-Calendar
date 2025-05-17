# SmartCalendar
## Функционал:

#### Пользователь:
- Регистрация пользователя
- Авторизация пользователя
- Изменение пользователя

#### Календарь:
- Создание календаря
- Именение календаря
- Удаление календаря
- Получение списка календарей

#### События:
- Создание события
- Изменение события
- Реакция на события

## Sequence диаграмма
![alt text](https://github.com/bolrob/Smart-Calendar/blob/master/sequence.png)

## C1 диаграмма
![alt text](https://github.com/bolrob/Smart-Calendar/blob/master/C1.png)

## C2 диаграмма
![alt text](https://github.com/bolrob/Smart-Calendar/blob/master/C2.png)

## DDL
```sql
create table public.users
(
    id bigserial primary key,
    username varchar(255) not null unique,
    phone varchar(12),
    email varchar(255),
    tg varchar(255) not null unique,
    password varchar not null,
    active boolean not null default true
)
create table public.events(
    id bigserial primary key,
    title varchar(255) not null,
    description varchar,
    address varchar,
    start_time timestamp not null,
    end_time timestamp not null,
    organizer_id bigint not null references public.users(id),
    calendar_id bigint not null references public.calendars(id)
    status varchar not null default('active'),
    average_rating decimal(3, 2) default(0.00)
)
create table public.registrations
(
    id bigserial primary key,
    user_id bigint not null references public.users(id),
    event_id bigint not null references public.events(id)
    registration_time timestamp not null,
)
create table public.reviews
(
    id bigserial primary key,
    user_id bigint not null references public.users(id),
    event_id bigint not null references public.events(id),
    rating_evaluation decimal(10, 2)
)
create table public.reminders
(
    id bigserial primary key,
    registration_id bigint not null references public.registrations(id)
    is_sent boolean default false
)
create table public.user_to_calendar(
    id bigserial primary key,
    calendar_id bigint not null references public.calendars(id),
    user_id bigint not null references public.users(id),
    access_type varchar not null default('ADMINISTRATOR')
)
create table public.calendars
(
    id bigserial primary key,
    calendar_name varchar(255) not null,
    is_public boolean not null default false,
    active boolean not null default true,
    teg varchar(255) not null unique,
    description varchar(255)
)
create table public.tokens
(
    id bigserial primary key,
    token varchar not null unique,
    user_id not null references public.users(id),
    revoked boolean default false
)

```
