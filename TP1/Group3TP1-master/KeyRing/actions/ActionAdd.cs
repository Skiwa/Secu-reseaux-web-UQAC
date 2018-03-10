using System;
using System.Collections.Generic;
using System.Text;
using KeyRing.model;
using KeyRing.utils;

namespace KeyRing.actions
{
    /**
     * Traite l'action d'ajouter un mot de passe au keyring d'un utilisateur
     */
    class ActionAdd : IAction
    {
        public string DoAction(ParameterBag parameters, KeyRingStore db)
        {
            /**
             * Ensuite, ils pourront ajouter des mots de passe associés à des tags comme ceci:
                dotnet run -a USERNAME MASTER_PASSWORD TAG PASSWORD
            */

            //Lire les paramètres
            string Username = parameters.GetNextParameter();
            string MasterPassword = parameters.GetNextParameter();
            string Tag = parameters.GetNextParameter();
            string Password = parameters.GetNextParameter();

            //Chercher l'utilisateur et tester le master password
            User user = db.Users.Find(Username);
            user.AssertMasterPassword(MasterPassword);

            //Construire le nouveau mot de passe
            Password pwd = new Password
            {
                Username = Username,
                Tag = Tag
            };
            pwd.EncryptPassword(Password, MasterPassword, user.Salt);

            //L'ajouter à la bd, et enregistrer
            db.Passwords.Add(pwd);
            db.SaveChanges();

            //Succès
            return "OK";
        }
    }
}
