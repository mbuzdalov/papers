program aa;
const
  maxm = 10+2;
  maxn = 100+5;
  maxs = 10000;
var
  f : array[0..maxn,0..maxs] of boolean;
  left:array[1..maxn] of longint;
  row,idrow:array[1..maxm] of longint;
  ship,id,belong:array[1..maxn] of longint;
  i,j,k,tmp,n,m:longint;
procedure Init;
  begin
	readln(n,m);
	for i:=1 to n do readln(ship[i]);
	for i:=1 to m do
	begin
		readln(row[i]);
		idrow[i]:=i;
	end;
	for i := 1 to m do
	for j := i+1 to m do if row[i]<row[j] then
	begin
		tmp:=row[i];row[i]:=row[j];row[j]:=tmp;
		tmp:=idrow[i];idrow[i]:=idrow[j];idrow[j]:=tmp;
	end;
	for i := 1 to n do id[i]:=i;
  end;
procedure print;
  begin
	for i := 1 to m do
	begin
		k:=0;
		for j:=1 to n do if idrow[belong[j]]=i then inc(k);
		writeln(k);
		for j:=1 to n do if idrow[belong[j]]=i then write(ship[j],' ');
		writeln;
	end;
	halt;
  end;
procedure dfs( x:longint );
var
  i,tot:longint;
  begin
	if x=0 then Print;

	//dp
	tot:=0;k:=0;
	for i := 1 to n do if belong[id[i]]=0 then
	begin
		inc(tot);left[tot]:=id[i];
		inc(k,ship[id[i]]);
	end;

    f[0,0]:=true;
	for i := 1 to tot do fillchar( f[i],k+1,false );
	
	k:=0;
	for i := 1 to tot do
	begin
		for j := 0 to k do if f[i-1,j] then
		begin
			f[i,j]:=true;f[i,j+ship[left[i]]]:=true;
		end;
		inc(k,ship[left[i]]);
	end;

	k:=0;
	for i := 1 to x do
	begin
		if not f[tot,row[i]] then exit;
		if not f[tot-1,row[i]] then inc(k);
	end;
	if k>1 then exit;

	k := row[x];
	for i := tot downto 1 do if not f[i-1,k] then
	begin
		belong[left[i]]:=x;
		dec( k,ship[left[i]] );
	end;

	dfs(x-1);

	for i := 1 to n do if belong[i]=x then belong[i]:=0;
  end;
procedure Main;
  begin
	f[0,0]:=true;
	while true do
	begin
		for i := 1 to n do
		begin
			j:=random(n)+1;
			k:=random(n)+1;
			tmp:=id[j];id[j]:=id[k];id[k]:=tmp;
		end;
		dfs(m);
	end;
  end;
begin
	Init;
	Main;
end.
