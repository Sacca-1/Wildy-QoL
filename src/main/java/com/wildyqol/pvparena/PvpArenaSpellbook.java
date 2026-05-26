package com.wildyqol.pvparena;

enum PvpArenaSpellbook
{
	STANDARD,
	ANCIENT,
	LUNAR;

	static PvpArenaSpellbook fromOwnVarbit(int value)
	{
		switch (value)
		{
			case 0:
				return ANCIENT;
			case 1:
				return STANDARD;
			case 2:
				return LUNAR;
			default:
				return null;
		}
	}

	static PvpArenaSpellbook fromOpponentVarbit(int value)
	{
		switch (value)
		{
			case 0:
				return STANDARD;
			case 1:
				return ANCIENT;
			case 2:
				return LUNAR;
			default:
				return null;
		}
	}
}
