package com.ettud.is74task4.Models.database;

import io.objectbox.annotation.Backlink;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToMany;
import io.objectbox.relation.ToOne;

@Entity
public class TermSource {
    @Id
    public long id;
    public ToOne<Term> term;
    public ToOne<Source> source;
    @Backlink(to = "termSource")
    public ToMany<Definition> definitions;
}
