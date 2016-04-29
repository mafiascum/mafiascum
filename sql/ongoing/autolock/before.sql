ALTER TABLE `phpbb_topics`
ADD `autolock_time` int(11) unsigned not null default '0',
ADD `autolock_input` varchar(32) default '',
ADD KEY `autolock_time`(`autolock_time`);
