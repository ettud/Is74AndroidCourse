package com.ettud.is74task4.Models.database;

import io.objectbox.annotation.Backlink;
import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.relation.ToMany;

@Entity
public class Term {
    @Id
    public long id;
    @Backlink(to = "term")
    public ToMany<TermSource> termSources;
    @Backlink(to = "term")
    public ToMany<TermSynonym> termSynonyms;
}
