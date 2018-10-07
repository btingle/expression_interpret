import operator as op
from math import sin, cos, tan, log, pow, sqrt, pi, e
pri_table, ops_table = {')':0,'+':1,'-':1,'*':2,'/':2,'^':3,'sin':4,'cos':4,'tan':4,'log':4,'sqrt':4,'(':5},  {'+':op.add,'-':op.sub,'*':op.mul,'/':op.truediv,'^':pow,'sin':sin,'cos':cos,'tan':tan,'log':log,'sqrt':sqrt}
def interp(raw_txt):
	registers, ops_queue, exp_text, num_buff, fnc_buff, raw_txt, k = [], [], [], "", "", "".join(raw_txt.split(" ")).replace("pi",str(pi)).replace("e",str(e)), 0
	while k < len(raw_txt):
		curr_op = raw_txt[k]
		if curr_op.isdigit() or curr_op is '.': 
			if not fnc_buff == "": return False
			num_buff,k = num_buff + curr_op, k+1
			continue
		if curr_op.isalpha(): 
			if not num_buff == "": return False
			fnc_buff,k = fnc_buff + curr_op, k+1
			continue
		if not num_buff == "": 
			exp_text.append(num_buff)
			num_buff = ""
		if not fnc_buff == "": 
			if fnc_buff in pri_table.keys(): curr_op = fnc_buff
			else: return False
			fnc_buff, k = "", k-1
		if curr_op in pri_table.keys(): 
			curr_prio,j = pri_table[curr_op],0
			while j < len(ops_queue):
				if ops_queue[j] is '(': 
					del ops_queue[j]
					break
				elif pri_table[ops_queue[j]] >= curr_prio: 
					exp_text.append(ops_queue[j])
					del ops_queue[j]
					j -= 1
				j += 1
			if not curr_op == ')':ops_queue.insert(0, curr_op)
		else: return False
		k += 1
	if not num_buff == "": exp_text.append(num_buff)
	for op in ops_queue: exp_text.append(op)
	if len(exp_text) == 0: return False
	for elem in exp_text:
		if elem[0].isdigit():registers.insert(0,float(elem))
		elif len(registers) > 1 and elem in [key for key in ops_table.keys()][0:5]: 
			registers.insert(0, ops_table[elem](registers[1], registers[0]))
			del registers[1:3]
		elif len(registers) > 0 and elem in [key for key in ops_table.keys()][5:]: 
			registers.insert(0,ops_table[elem](registers[0]))
			del registers[1]
		else: return False
	print(registers[0])
	return True
			
	
if __name__ == "__main__":
	while(interp(input(">> "))):
		pass