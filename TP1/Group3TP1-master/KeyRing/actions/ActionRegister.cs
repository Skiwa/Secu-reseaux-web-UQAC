using System;
using System.Collections.Generic;
using System.Text;
using KeyRing.utils;
using KeyRing.model;

namespace KeyRing.actions
{
    /**
     * Traite l'action d'enregistrement d'un utilisateur
     */
    class ActionRegister : IAction
    {
        public string DoAction(ParameterBag parameters, KeyRingStore db)
        {
            /**
             * Avant toute chose le (ou les) utilisateur(s) de votre application devront s’enregistrer sur votre
                application avec la commande suivante :
                dotnet run -r USERNAME MASTER_PASSWORD
            */

            //Lire les paramètres
            string Username = parameters.GetNextParameter();
            string MasterPassword = parameters.GetNextParameter();

            //Créer l'utilisateur
            User user = new User { Username = Username };
            user.SetMasterPassword(MasterPassword);

            //L'ajouter à la bd, et enregistrer
            db.Users.Add(user);
            db.SaveChanges();

            //Succès
            return "OK";
        }
    }
}
