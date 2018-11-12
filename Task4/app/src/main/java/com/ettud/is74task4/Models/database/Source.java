package com.ettud.is74task4.Models.database;

import io.objectbox.annotation.Backlink;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToMany;

@Entity
public class Source {
    @Id
    public long id;
    public String name;
    public int priority;

    @Backlink(to = "source")
    public ToMany<TermSource> termSources;
}
