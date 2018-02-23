.open Transactions.db

PRAGMA foreign_keys = ON;

create table categories (
categoriesId INTEGER PRIMARY KEY AUTOINCREMENT,
name text not null, 
goal real not null, 
regex text, 
goalCount int not null
);

create table transactions (
transactionsId INTEGER PRIMARY KEY AUTOINCREMENT,
date int not null, 
amount real not null, 
type text not null, 
name text not null, 
categoryId int not null, 
foreign key(categoryId) references categories(categoriesId)
);

insert into categories values (null, "None", 0, "", 0);
insert into categories values (null, "Pay", 0, "", 0);
insert into categories values (null, "NO REPORT", 0, "", 0);