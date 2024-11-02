-- 管理员创建三个账号
create user xx identified by xx;
grant connect , resource to xx;

create user yy identified by yy;
grant connect , resource to yy;

create user zz identified by zz;
grant connect , resource to zz;

--使用xx登录
create table singer(
 id number(10) primary key,
 name varchar2(36)
);
grant select on singer to zz;

insert into singer(id, name ) values (1, '刘德华');
insert into singer(id, name ) values (2, '张学友');
insert into singer(id, name ) values (3, '周华健');
insert into singer(id, name ) values (4, '李宗盛');


--使用yy登录
 
create table song(
  id number(10) primary key,
  singer_id number(10),
  name varchar2(64)
);
grant select on song to zz;

insert into song(id, singer_id,name) values (1,1,'忘情水');
insert into song(id, singer_id,name) values (2,1,'冰雨');
insert into song(id, singer_id,name) values (3,1,'爱你一万年');
insert into song(id, singer_id,name) values (4,1,'天意');
insert into song(id, singer_id,name) values (5,1,'练习');


insert into song(id, singer_id,name) values (6,2,'她来听我的演唱会');
insert into song(id, singer_id,name) values (7,2,'吻别');
insert into song(id, singer_id,name) values (8,2,'一千个伤心的理由');


insert into song(id, singer_id,name) values (9,3,'朋友');
insert into song(id, singer_id,name) values (10,3,'难念的经');


insert into song(id, singer_id,name) values (11,4,'真心英雄');
insert into song(id, singer_id,name) values (12,4,'凡人歌');
insert into song(id, singer_id,name) values (13,4,'山丘');
insert into song(id, singer_id,name) values (14,4,'漂洋过海来看你');

--   临时表
create global temporary table song_tmp(
  song_id number(10),
  song_name varchar2(64)
)
on commit delete rows;

--使用zz登录

create table play(
  song_id number(10),
  play_time  date default sysdate

);
--   随机造一些播放数据 
INSERT INTO play (song_id)
SELECT TRUNC(DBMS_RANDOM.VALUE(1, 15))  FROM DUAL;
commit;

select * from play;

--   临时表
create global temporary table song_play_count_tmp(
  song_id number(10),
  play_times number(10),
  song_name varchar2(64),
  singer_name varchar2(64)
)
on commit delete rows;

create  global temporary table song_tmp(
  id number(10),
  name varchar2(64),
  singer_id number(10),
  singer_name varchar2(64)
)
on commit delete rows;

--需求：
--统计最近7天播放量前三的歌曲名字及其演唱者

--1.calcite/dblink的解决方案
select *
  from (select song, singer, play_times
          from (select song.name as song,
                       singer.name as singer,
                       count(1) as play_times
                  from xx.singer singer, yy.song song, zz.play play
                 where singer.id = song.singer_id
                   and play.song_id = song.id
                   and play.play_time between sysdate - 7 and sysdate
                 group by song.name, singer.name)
         order by play_times desc)
 where rownum <= 3;
 

--2.跨库+临时表解决方案
zzSql：
insert into song_play_count_tmp (song_id,play_times)  
select song_id,play_times from (
select song_id, play_times from (
select song_id , count(1) as play_times from  zz.play play
where  play.play_time between sysdate - 7 and sysdate
group by play.song_id ) order by play_times desc
)  where rownum <= 3


calcSql:
    ConnectZZ  MaxTotal? => DataSource(-->)
    ConnectXX  MaxTotal?
SELECT * FROM zz.song_play_count_tmp, xx.singer 



