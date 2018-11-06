package com.ettud.is74task4.Models;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class Term {
    @Id
    public long id;
    public String term;
    public String definition;
}
