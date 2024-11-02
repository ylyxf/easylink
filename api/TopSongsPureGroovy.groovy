import org.slf4j.Logger;

import com.jirfox.smartlink.utils.DataSyncer

import groovy.sql.Sql

//http://127.0.0.1:1997/api/TopSongsPureGroovy?beforeDays=7&topCount=3
Sql xxSql = Sql.newInstance(_ds['xx']);
Sql yySql = Sql.newInstance(_ds['yy']);
Sql zzSql = Sql.newInstance(_ds['zz']);

Sql calcSql = Sql.newInstance(_calc);

def result = null;
zzSql.withTransaction {
	
	//在本地库统计好
	zzSql.execute('''
		insert into song_play_count_tmp (song_id,play_times)  
		select song_id,play_times from (
		select song_id, play_times from (
		select song_id , count(1) as play_times from  zz.play play
		where  play.play_time between sysdate - :beforeDays and sysdate
		group by play.song_id ) order by play_times desc
		)  where rownum <= :topCount
	''',[beforeDays:_params['beforeDays'],topCount:_params['topCount']]);
	
	//将歌曲id送到目标库，获得song_name后取回
	result = zzSql.rows("""
		select song_id from song_play_count_tmp
	""");
	yySql.withTransaction {
		DataSyncer.doWriteData(yySql, "song_tmp", result);
		result = yySql.rows("""
			select t.song_id as id ,s.name,s.singer_id from song_tmp t ,song s where t.song_id = s.id
		""");
		DataSyncer.doWriteData(zzSql, "song_tmp", result);
	}
	//取回后更新结果表的歌曲名称和歌手ID
	zzSql.execute("""update SONG_PLAY_COUNT_TMP pc set (pc.song_name,pc.singer_id ) =
		(select t.name,t.singer_id from song_tmp t where t.id = pc.song_id)
	""");
	
	result = zzSql.rows("""
		select distinct singer_id as id from SONG_PLAY_COUNT_TMP
	""");
	//将歌手id送到目标库，获得singer_name后取回
	xxSql.withTransaction {
		DataSyncer.doWriteData(xxSql, "singer_tmp", result);
		result = xxSql.rows("""
			select distinct t.id as singer_id ,s.name as singer_name from singer_tmp t ,singer s where t.ID = s.id
		""");
		zzSql.execute("delete from SONG_TMP");
		DataSyncer.doWriteData(zzSql, "SONG_TMP", result);
	}
	
	zzSql.execute("""update SONG_PLAY_COUNT_TMP pc set (pc.SINGER_NAME ) =
		(select t.singer_name from song_tmp t where t.singer_id = pc.singer_id)
	""");
	
	result = zzSql.rows("""
		select * from SONG_PLAY_COUNT_TMP
	""");
	
}

return result;