.open tool_platform.db

create table xshell_config(
  id integer primary key autoincrement not null,
  jump_ip text not null,
  jump_port integer not null,
  jump_pwd text not null,
  target_ip text not null,
  docker_id text not null,
  comment text not null,
  create_time text not null,
  modify_time text not null
);