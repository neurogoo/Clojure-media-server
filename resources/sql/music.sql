-- sql/music.sql
-- Songs and albums

-- :name create-album-table
-- :command :execute
-- :result :raw
-- :doc Create album table
create table albums (
  id         integer primary key, 
  name       varchar,
  created_at timestamp not null default current_timestamp
)

-- :name drop-albums-table :!
-- :doc Drop albums table if exists
drop table if exists albums

-- A :result value of :n below will return affected row count:
-- :name insert-album :! :n
-- :doc Insert a single character
insert into albums (name)
values (:name)

-- :name insert-albums :! :n
-- :doc Insert multiple albums with :tuple* parameter type
insert into albums (name)
values :tuple*:albums

-- :name get-all-albums :? :*
-- :doc Get all albums
select * from albums

-- :name create-song-table
-- :command :execute
-- :result :raw
-- :doc Create song table
create table songs (
  id         integer primary key,
  album_id   integer,
  name       varchar,
  path       varchar,
  created_at timestamp not null default current_timestamp
)

-- :name drop-songs-table :!
-- :doc Drop songs table if exists
drop table if exists songs

-- A :result value of :n below will return affected row count:
-- :name insert-song :! :n
-- :doc Insert a single character
insert into songs (album_id, name, path)
values (:album_id, :name, :path)

-- :name insert-songs :! :n
-- :doc Insert multiple songs with :tuple* parameter type
insert into songs (album_id, name, path)
values :tuple*:songs

-- :name get-all-songs :? :*
-- :doc Get all songs
select * from songs
