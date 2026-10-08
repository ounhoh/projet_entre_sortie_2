package com.cese.process_entree_sortie.domain.Agent.model;

public enum Role  {
    Agent(1),
    Manager(2),
    Admin(3),
    Prestataire(4),
    Conseiller(5);
 private final int value;

    private Role(int setvalue)
    {
      value = setvalue;
    }

    public int value()
   {
       return value;
   }
}
