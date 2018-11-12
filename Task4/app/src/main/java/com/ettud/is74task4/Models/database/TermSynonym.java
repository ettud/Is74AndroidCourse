package com.ettud.is74task4.Models.database;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToOne;

@Entity
public class TermSynonym {
    @Id
    public long id;
    public String name;
    public ToOne<Term> term;
}
