package trabalho;

import java.util.ArrayList;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class Model {
	IloCplex cplex; 
	
	
	public void creatModel(float[][] B, float[][] H, float[][] C, float[][] S, float[][] I,  float[] L, float[] c, float[] m, float[] q, String path) throws IloException {
		cplex = new IloCplex();
		
		//Instanciação das variaveis 
		ArrayList<IloNumVar>[] y = new ArrayList[B.length];
		ArrayList<ArrayList<IloNumVar>>[] x = new ArrayList[B.length]; 
		
		ArrayList<IloNumVar>[] u = new ArrayList[C.length];
		ArrayList<ArrayList<IloNumVar>>[] t = new ArrayList[C.length];
		
		
		//Inicializar as variaveis x e y referente as salas de aula
		for (int i = 0; i < B.length; i++) {
			y[i] = new ArrayList<IloNumVar>();
			x[i] = new ArrayList<ArrayList<IloNumVar>>();
			
			for(int k = 0; k < H[i].length; k++) {
				x[i].add(new ArrayList<IloNumVar>());
				
				for(int j = 0; j < S[i].length; j++) {
					x[i].get(k).add(cplex.boolVar( "x(" + i + "," + k + ","+ j+")"));
				}	
			}
			
			
			for(int j = 0; j < S[i].length; j++) {
				y[i].add(cplex.boolVar( "y(" + i + "," + j + ")"));
			}		
		}
		
		//Inicializar as variaveis u e t referente aos laboratorios		
		for (int i = 0; i < C.length; i++) {
			u[i] = new ArrayList<IloNumVar>();
			t[i] = new ArrayList<ArrayList<IloNumVar>>();
			
			for(int k = 0; k < H[i].length; k++) {
				t[i].add(new ArrayList<IloNumVar>());
				
				for(int j = 0; j < I[i].length; j++) {
					t[i].get(k).add(cplex.boolVar( "t(" + i + "," + k + ","+ j+")"));
				}	
			}
			
			
			for(int j = 0; j < I[i].length; j++) {
				u[i].add(cplex.boolVar( "u(" + i + "," + j + ")"));
			}		
		}
		
		
		//Add Restrição 2
		for (int i = 0; i < B.length; i++) {
			for(int k = 0; k < H[i].length; k++) {
				IloLinearNumExpr r2 = cplex.linearNumExpr();
				for(int j = 0; j < S[i].length; j++) {
					r2.addTerm(1, x[i].get(k).get(j));
				}
				cplex.addEq(r2, 1);
			}
		}
		
		//Add Restrição 3
		for (int i = 0; i < C.length; i++) {
			for(int k = 0; k < H[i].length; k++) {
				IloLinearNumExpr r3 = cplex.linearNumExpr();
				for(int j = 0; j < I[i].length; j++) {
					r3.addTerm(1, t[i].get(k).get(j));
				}
				cplex.addEq(r3, 1);
			}
		}
		
		//Add Restrição 4
		for (int j = 0; j < L.length; j++) {
			for(int k = 0; k < H.length; k++) {
				IloLinearNumExpr r4 = cplex.linearNumExpr();
				
				for(int i = 0; i < B[k].length; i++) {										
					r4.addTerm(1, x[i].get(k).get(j));
				}
				
				for(int i = 0; i < C[k].length; i++) {										
					r4.addTerm(1, t[i].get(k).get(j));
				}
				
				cplex.addLe(r4, 1);
			}
		}
		
		//Add Restrição 5
		for (int i = 0; i < B.length; i++) {
			for(int k = 0; k < H[i].length; k++) {
				for(int j = 0; j < S[i].length; j++) {
					IloLinearNumExpr r5 = cplex.linearNumExpr();
					r5.addTerm(1, x[i].get(k).get(j));
					r5.addTerm(-1, y[i].get(j));
					
					cplex.addEq(r5, 0);
				}
			}
		}
		
		//Add Restrição 6
		for (int i = 0; i < C.length; i++) {
			for(int k = 0; k < H[i].length; k++) {
				for(int j = 0; j < I[i].length; j++) {
					IloLinearNumExpr r6 = cplex.linearNumExpr();
					r6.addTerm(1, t[i].get(k).get(j));
					r6.addTerm(-1, u[i].get(j));
					
					cplex.addEq(r6, 0);
				}
			}
		}
		
		//Add Funcao Objetivo 1
		IloLinearNumExpr fo = cplex.linearNumExpr();
		for (int i = 0; i < B.length; i++) {
			for(int k = 0; k < H[i].length; k++) {
				for(int j = 0; j < S[i].length; j++) {
					fo.addTerm(c[j], x[i].get(k).get(j));
				}
			}
		}
		
		for (int i = 0; i < C.length; i++) {
			for(int k = 0; k < H[i].length; k++) {
				for(int j = 0; j < I[i].length; j++) {
					fo.addTerm(c[j] + (q[i]*m[j]), t[i].get(k).get(j));
				}
			}
		}
		
		cplex.addMinimize(fo);
		
		//Criar o model.lp		
		cplex.exportModel(path);
		
		//Resolver o problema
		cplex.solve();
		
	}
}
