insert into users(userName) values('CPU');
insert into users(userName) values('ほんだ');
insert into users(userName) values('いがき');

insert into matches(user1,user2,user1Hand,user2Hand) values(2,1,'Gu','Choki');
insert into matches(user1,user2,user1Hand,user2Hand) values(2,1,'Gu','Gu');
insert into matches(user1,user2,user1Hand,user2Hand) values(2,1,'Gu','Pa');

insert into matchinfo(user1,user2,user1Hand,isActive) values(1,2,'Gu',false);
insert into matchinfo(user1,user2,user1Hand,isActive) values(2,3,'Choki',false);
