package com.wildyqol.pvparena;

enum PvpArenaBuild
{
	MAIN,
	ZERK,
	PURE;

	static PvpArenaBuild fromVarbit(int value)
	{
		switch (value)
		{
			case 0:
				return MAIN;
			case 1:
				return ZERK;
			case 2:
				return PURE;
			default:
				return null;
		}
	}
}
