using System;
using System.Collections.Generic;
using System.Text;
using KeyRing.model;
using KeyRing.utils;

namespace KeyRing.actions
{
    /**
     * Traite l'action de divulgation d'information pour aider le prof à corriger
     */
    class ActionTell : IAction
    {
        public string DoAction(ParameterBag parameters, KeyRingStore db)
        {
            //Lire le nom d'utilisateur
            string Username = parameters.GetNextParameter();

            if (!parameters.RemainParameters())
            {
                /**
                 * Pour faciliter la correction, vous devez fournir deux fonctions d’affichage très simples :
                dotnet run -t USERNAME doit retourner le mot de passe hashé et le sel cryptographique enregistrés
                dans la base de données de l’utilisateur au format suivant : SALT:HASH
                */

                //Chercher l'utilisateur
                User user = db.Users.Find(Username);

                //Récupérer le salt et le hash (tous 2 en base 64)
                string salt = user.Salt;
                string hash = user.MasterPasswordHash;

                //Retourner les information de l'utilisateur
                return salt + ":" + hash;
            }
            else
            {
                /**
                 * dotnet run -t USERNAME TAG doit retourner la version chiffrée encodée en Base64 du mot de passe
                correspondant au tag
                */

                //Lire les autres paramètres
                string Tag = parameters.GetNextParameter();

                //Chercher le password
                Password pwd = db.Passwords.Find(Username, Tag);

                //Récupérer le cipher du mot de passe (en bas 64)
                string cipher = pwd.StoredPassword;

                //Retourner le cipher récupéré
                return cipher;
            }
        }
    }
}
