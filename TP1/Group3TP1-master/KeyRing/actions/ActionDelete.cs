using System;
using System.Collections.Generic;
using System.Text;
using KeyRing.model;
using KeyRing.utils;

namespace KeyRing.actions
{
    /**
     * Traite l'action du suppression d'un mot de passe du keyring
     */
    class ActionDelete : IAction
    {
        public string DoAction(ParameterBag parameters, KeyRingStore db)
        {
            /*
             * La suppression, fonction importante de ce type d’application, se fera de la manière suivante :
            dotnet run -d USERNAME MASTER_PASSWORD TAG
            */

            //Lire les paramètres
            string Username = parameters.GetNextParameter();
            string MasterPassword = parameters.GetNextParameter();
            string Tag = parameters.GetNextParameter();

            //Chercher le password
            Password pwd = db.Passwords.Find(Username, Tag);
            //Charger l'utilisateur associé et tester le Master Password
            pwd.LoadUser(db).AssertMasterPassword(MasterPassword);

            //Supprimer le mot de passe de la bd et sauvegarder
            db.Passwords.Remove(pwd);
            db.SaveChanges();

            //Succès
            return "OK";
        }
    }
}
