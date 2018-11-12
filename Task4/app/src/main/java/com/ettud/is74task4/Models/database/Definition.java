package com.ettud.is74task4.Models.database;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToOne;

@Entity
public class Definition {
    @Id
    public long id;
    public String text;
    public ToOne<TermSource> termSource;
}
