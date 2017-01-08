-- sql/music.sql
-- Songs and albums

-- :name create-album-table
-- :command :execute
-- :result :raw
-- :doc Create album table
create table albums (
  id         integer primary key, 
  name       varchar(40),
  path       varchar(40),
  created_at timestamp not null default current_timestamp
)

/* The create-character-table definition above uses the full,
long-hand "-- :key :value" syntax to specify the :command and
:result.  We can save some typing by using the short-hand notation
as the second and (optionally) third values for the :name.  Below, the
:! is equivalent to ":command :!", where :! is an alias for
:execute.  The default :result is :raw when not specified, so
there is no need to specify it as the third value. */

-- :name drop-albums-table :!
-- :doc Drop albums table if exists
drop table if exists albums

-- A :result value of :n below will return affected row count:
-- :name insert-album :! :n
-- :doc Insert a single character
insert into albums (name, path)
values (:name, :path)

-- :name insert-albums :! :n
-- :doc Insert multiple albums with :tuple* parameter type
insert into albums (name, path)
values :tuple*:albums


-- :name get-all-albums :? :*
-- :doc Get all albums
select * from albums
