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

-- :name get-album-by-id :? :1
-- :doc Get album by id
select * from albums
where id = :id

-- :name create-albums-virtual-table
-- :command :execute
-- :result :raw
-- :doc Create album virtual table
create virtual table albums_virtual_table using fts4(content="albums", name)

-- A :result value of :n below will return affected row count:
-- :name populate-albums-virtual-table :! :n
-- :doc Insert albums table to the albums virtual table
insert into albums_virtual_table (docid, name)
select id, name from albums 

-- :name drop-albums-virtual-table :!
-- :doc Drop albums table if exists
drop table if exists albums_virtual_table

-- :name search-albums-virtual-table :? :*
-- :doc Search albums virtual table with search-term
select docid from albums_virtual_table
where albums_virtual_table match :search-term

-- :name create-artist-table
-- :command :execute
-- :result :raw
-- :doc Create artist table
create table artists (
  id         integer primary key,
  name       varchar,
  created_at timestamp not null default current_timestamp
)

-- :name drop-artist-table :!
-- :doc Drop artist table if exists
drop table if exists artists

-- A :result value of :n below will return affected row count:
-- :name insert-artist :! :n
-- :doc Insert a single artist
insert into artists (name)
values (:name)

-- :name insert-artists :! :n
-- :doc Insert multiple songs with :tuple* parameter type
insert into artists (name)
values :tuple*:artists

-- :name get-all-artists :? :*
-- :doc Get all artists
select * from artists

-- :name get-artist-by-id :? :1
-- :doc Get artist by id
select * from artists
where id = :id

-- :name create-artists-virtual-table
-- :command :execute
-- :result :raw
-- :doc Create artist virtual table
create virtual table artists_virtual_table using fts4(content="artists", name)

-- A :result value of :n below will return affected row count:
-- :name populate-artists-virtual-table :! :n
-- :doc Insert artist table to the artist virtual table
insert into artists_virtual_table (docid, name)
select id, name from artists

-- :name drop-artists-virtual-table :!
-- :doc Drop artists table if exists
drop table if exists artists_virtual_table

-- :name search-artists-virtual-table :? :*
-- :doc Search artists virtual table with search-term
select docid from artists_virtual_table
where artists_virtual_table match :search-term

-- :name create-song-table
-- :command :execute
-- :result :raw
-- :doc Create song table
create table songs (
  id         integer primary key,
  album_id   integer,
  artist_id  integer,
  name       varchar,
  path       varchar,
  created_at timestamp not null default current_timestamp,
  FOREIGN KEY(album_id) REFERENCES albums(id),
  FOREIGN KEY(artist_id) REFERENCES artists(id)
)

-- :name drop-songs-table :!
-- :doc Drop songs table if exists
drop table if exists songs

-- A :result value of :n below will return affected row count:
-- :name insert-song :! :n
-- :doc Insert a single character
insert into songs (album_id, artist_id, name, path)
values (:album_id, :artist_id, :name, :path)

-- :name insert-songs :! :n
-- :doc Insert multiple songs with :tuple* parameter type
insert into songs (album_id, artist_id, name, path)
values :tuple*:songs

-- :name get-all-songs :? :*
-- :doc Get all songs
select * from songs

-- :name get-song-by-id :? :1
-- :doc Get character by id
select * from songs
where id = :id

-- :name get-songs-by-album-id :? :*
-- :doc Get character by album id
select * from songs
where album_id = :album_id

-- :name get-songs-by-artist-id :? :*
-- :doc Get character by artist id
select * from songs
where artist_id = :artist_id

-- :name create-songs-virtual-table
-- :command :execute
-- :result :raw
-- :doc Create song virtual table
create virtual table songs_virtual_table using fts4(content="songs", name, path)

-- A :result value of :n below will return affected row count:
-- :name populate-songs-virtual-table :! :n
-- :doc Insert songs table to the songs virtual table
insert into songs_virtual_table (docid, name, path)
select id, name, path from songs

-- :name drop-songs-virtual-table :!
-- :doc Drop songs table if exists
drop table if exists songs_virtual_table

-- :name search-songs-virtual-table :? :*
-- :doc Search songs virtual table with search-term
select docid from songs_virtual_table
where songs_virtual_table match :search-term
