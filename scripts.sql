--таблица данных обычных пользователей, которые могут просматривать мероприятия и регистрироваться на них
--у пользователей есть имя пользователя, телефон, почта и тг
create table public.users
(
    id bigserial primary key,
    username varchar(255) not null unique,
    phone varchar(12) unique,
    email varchar(255) unique,
    tg varchar(255) unique,
)

--таблица с данными организаторов мероприятий (под организатором понимается какая-то компания или отдельный человек)
--в ней хранятся имя организации, контактный телефон, почта
create table public.organizers
(
    id bigserial primary key,
    organization_name varchar(255) not null,
    contact_phone varchar(12) unique,
    email varchar(255) unique,
)

--таблица с описанием мероприятий (название, краткое описание мероприятия, его адрес проведения, координаты,
--время начала и конца, кто организатор, активно ли мероприятие (то есть можно ли на него зарегистрироваться) и средний рейтинг от пользователей
create table public.events(
    id bigserial primary key,
    title varchar(255) not null,
    description varchar,
    adress varchar,
    latitude decimal(10, 8) not null,
    longitude decimal(10, 8) not null,
    start_time timestamp not null,
    end_time timestamp not null,
    organizer_id bigint not null references public.organizers(id),
    status varchar not null default('active'),
    average_raiting decimal(3, 2) default(0.00)
)

--таблица с данными о регистрациях пользователей на конкретное мероприятие
create table public.registrations
(
    id bigserial primary key,
    user_id bigint not null references public.users(id),
    event_id bigint not null references public.events(id)
    registration_time timestamp not null,
)

--таблица отзывов(какой пользователь оставил, какому мероприятию, какую оценку поставил)
create table public.reviews
(
    id bigserial primary key,
    user_id bigint not null references public.users(id),
    event_id bigint not null references public.events(id),
    raiting evaluation decimal(10, 2)
)

--таблица с данными о напоминаниях (на какое событие, какому пользователю, время отправки напоминания и статус отправки (было отправлено или нет)
create table public.reminders
(
    id bigserial primary key,
    event_id bigint not null references public.events(id),
    user_id bigint not null references public.users(id),
    reminder_time timestamp not null,
    is_sent boolean default false
)

--позднее будет создана таблица для календарей
--она будет создана после того, как мы поймем как интегрироваться с внешними серверами,
--такими как гугл календарь, яндекс календарь и прочее