using KeyRing.model;
using KeyRing.utils;
using System;
using System.Collections.Generic;
using System.Text;

namespace KeyRing.actions
{
    interface IAction
    {
        string DoAction(ParameterBag parameters, KeyRingStore db);
    }
}
