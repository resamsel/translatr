# --- !Ups

alter table message add column word_count int;
alter table locale add column word_count int;
alter table key add column word_count int;
alter table project add column word_count int;

# --- !Downs

alter table message drop column word_count;
alter table locale drop column word_count;
alter table key drop column word_count;
alter table project drop column word_count;
