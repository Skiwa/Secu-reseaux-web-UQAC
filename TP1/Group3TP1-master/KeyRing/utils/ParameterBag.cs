using System;
using System.Collections.Generic;
using System.Text;

namespace KeyRing.utils
{
    class ParameterBag
    {
        //Ensemble des paramètres dans le sac
        private string[] parameters;
        //Index du prochain paramètre à lire
        private int nextParameterIndex = 0;

        public ParameterBag(string[] args)
        {
            //Initialiser le tableau des paramètres avec le tableau des arguments de la ligne de commande
            parameters = args;
        }

        /**
         * Retourne le prochain parametre qui n'a pas encore été lu
         */
        public string GetNextParameter()
        {
            return parameters[nextParameterIndex++];
        }

        /**
         * Indique s'il reste au moins 1 paramètre à lire
         */
        public bool RemainParameters()
        {
            return (parameters.Length - nextParameterIndex) > 0;
        }
    }
}
