using KeyRing.model;
using System;
using Microsoft.EntityFrameworkCore;
using KeyRing.utils;
using KeyRing.actions;

namespace KeyRing
{
    class Program
    {
        /**
         * Affiche une ligne sur la console
         */
        static void ShowLine(string line)
        {
            Console.WriteLine(line);
        }

        /**
         * Retourne une action du bon type, selon le flag reçu
         */
        static IAction GetActionByFlag(string flag)
        {
            if (flag.Equals("-r"))
            {
                return new ActionRegister();
            }
            else if (flag.Equals("-a"))
            {
                return new ActionAdd();
            }
            else if (flag.Equals("-g"))
            {
                return new ActionGet();
            }
            else if (flag.Equals("-d"))
            {
                return new ActionDelete();
            }
            else if (flag.Equals("-t"))
            {
                return new ActionTell();
            }
            else
            {
                return null;
            }
        }

        static void Main(string[] args)
        {
            //Créer un sac de paramètres qu'on initialise avec les arguments de la ligne de commande
            ParameterBag parameters = new ParameterBag(args);

            //Lire le flag correspondant à l'action à effectuer
            string flag = parameters.GetNextParameter();

            //Et récupérer l'action correspondante au flag
            IAction action = GetActionByFlag(flag);

            //NOTE: On ne fait aucune validation, on laisse les exceptions se lancer, et on affiche ERROR si on en attrape une
            try
            {
                //Ouvrir la base de donnée, elle se fermera automatiquement èa la fin du bloc "using"
                using (var db = new KeyRingStore())
                {
                    //Exécuter l'action et récupérer le résultat à afficher
                    string toShow = action.DoAction(parameters, db);

                    //Afficher le résultat
                    ShowLine(toShow);
                }
            }
            catch (Exception)
            {
                //NOTE: Une erreur ne se produira jamais après avoir écrit un texte légitime
                //Afficher le message d'erreur
                ShowLine("ERROR");
            }
        }
    }
}
