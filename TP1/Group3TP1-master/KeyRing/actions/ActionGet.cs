using System;
using System.Collections.Generic;
using System.Text;
using KeyRing.model;
using KeyRing.utils;

namespace KeyRing.actions
{
    /**
     * Traite l'action d'obtenir (lire) un mot de passe enregistré dans le keyring
     */
    class ActionGet : IAction
    {
        public string DoAction(ParameterBag parameters, KeyRingStore db)
        {
            /**
             * Évidemment ils doivent pouvoir récupérer les mots de passe avec la commande (cette commande
                affiche sur la console le mot de passe en clair) :
                dotnet run -g USERNAME MASTER_PASSWORD TAG
            */

            //Lire les paramètres
            string Username = parameters.GetNextParameter();
            string MasterPassword = parameters.GetNextParameter();
            string Tag = parameters.GetNextParameter();

            //Chercher le password
            Password pwd = db.Passwords.Find(Username, Tag);
            //Charger l'utilisateur associé et tester le Master Password
            pwd.LoadUser(db).AssertMasterPassword(MasterPassword);

            //Récupérer le mot de passe
            string Password = pwd.DecryptPassword(MasterPassword, pwd.User.Salt);

            //Retourner le mot de passe déchiffré
            return Password;
        }
    }
}
