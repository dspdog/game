






///////////////////////////////////////////

namespace Simplify
{
	// Global Variables & Strctures

	struct Triangle { int v[3];double err[4];int deleted,dirty;vec3f n; };
	struct Vertex { vec3f p;int tstart,tcount;SymetricMatrix q;int border;};
	struct Ref { int tid,tvertex; };
	std::vector<Triangle> triangles;
	std::vector<Vertex> vertices;
	std::vector<Ref> refs;

	// Helper functions

	double vertex_error(SymetricMatrix q, double x, double y, double z);
	double calculate_error(int id_v1, int id_v2, vec3f &p_result);
	bool flipped(vec3f p,int i0,int i1,Vertex &v0,Vertex &v1,std::vector<int> &deleted);
	void update_triangles(int i0,Vertex &v,std::vector<int> &deleted,int &deleted_triangles);
	void update_mesh(int iteration);
	void compact_mesh();
	//
	// Main simplification function 
	//
	// target_count  : target nr. of triangles
	// agressiveness : sharpness to increase the threashold.
	//                 5..8 are good numbers
	//                 more iterations yield higher quality
	//
	void simplify_mesh(int target_count, double agressiveness=7)
	{
		// init
		printf("%s - start\n",__FUNCTION__);
		int timeStart=timeGetTime();

		loopi(0,triangles.size()) triangles[i].deleted=0;
		
		// main iteration loop 

		int deleted_triangles=0; 
		std::vector<int> deleted0,deleted1;
		int triangle_count=triangles.size();
		
		loop(iteration,0,100) 
		{
			// target number of triangles reached ? Then break
			printf("iteration %d - triangles %d\n",iteration,triangle_count-deleted_triangles);
			if(triangle_count-deleted_triangles<=target_count)break;

			// update mesh once in a while
			if(iteration%5==0) 
			{
				update_mesh(iteration);
			}

			// clear dirty flag
			loopi(0,triangles.size()) triangles[i].dirty=0;
			
			//
			// All triangles with edges below the threshold will be removed
			//
			// The following numbers works well for most models.
			// If it does not, try to adjust the 3 parameters
			//
			double threshold = 0.000000001*pow(double(iteration+3),agressiveness);

			// remove vertices & mark deleted triangles			
			loopi(0,triangles.size())
			{				
				Triangle &t=triangles[i];
				if(t.err[3]>threshold) continue;
				if(t.deleted) continue;
				if(t.dirty) continue;
				
				loopj(0,3)if(t.err[j]<threshold) 
				{
					int i0=t.v[ j     ]; Vertex &v0 = vertices[i0]; 
					int i1=t.v[(j+1)%3]; Vertex &v1 = vertices[i1];

					// Border check
					if(v0.border != v1.border)  continue;

					// Compute vertex to collapse to
					vec3f p;
					calculate_error(i0,i1,p);

					deleted0.resize(v0.tcount); // normals temporarily
					deleted1.resize(v1.tcount); // normals temporarily

					// dont remove if flipped
					if( flipped(p,i0,i1,v0,v1,deleted0) ) continue;
					if( flipped(p,i1,i0,v1,v0,deleted1) ) continue;

					// not flipped, so remove edge												
					v0.p=p;
					v0.q=v1.q+v0.q;
					int tstart=refs.size();

					update_triangles(i0,v0,deleted0,deleted_triangles);
					update_triangles(i0,v1,deleted1,deleted_triangles);
						
					int tcount=refs.size()-tstart;
				
					if(tcount<=v0.tcount)
					{
						// save ram
						if(tcount)memcpy(&refs[v0.tstart],&refs[tstart],tcount*sizeof(Ref));
					}
					else
						// append
						v0.tstart=tstart;

					v0.tcount=tcount;
					break;
				}

			}
		}

		// clean up mesh
		compact_mesh();

		// ready
		int timeEnd=timeGetTime();
		printf("%s - %d/%d %d%% removed in %d ms\n",__FUNCTION__,
			triangle_count-deleted_triangles,
			triangle_count,deleted_triangles*100/triangle_count,
			timeEnd-timeStart);
		
	}


	// compact triangles, compute edge error and build reference list

	void update_mesh(int iteration)
	{
		if(iteration>0) // compact triangles
		{
			int dst=0;
			loopi(0,triangles.size())
			if(!triangles[i].deleted)
			{
				triangles[dst++]=triangles[i];
			}
			triangles.resize(dst);
		}
		//
		// Init Quadrics by Plane & Edge Errors
		//
		// required at the beginning ( iteration == 0 )
		// recomputing during the simplification is not required,
		// but mostly improves the result for closed meshes
		//
		if( iteration == 0 )
		{
			loopi(0,vertices.size())
			vertices[i].q=SymetricMatrix(0.0);

			loopi(0,triangles.size())
			{
				Triangle &t=triangles[i];
				vec3f n,p[3];
				loopj(0,3) p[j]=vertices[t.v[j]].p;
				n.cross(p[1]-p[0],p[2]-p[0]);
				n.normalize();
				t.n=n;
				loopj(0,3) vertices[t.v[j]].q =
					vertices[t.v[j]].q+SymetricMatrix(n.x,n.y,n.z,-n.dot(p[0]));
			}
			loopi(0,triangles.size())
			{
				// Calc Edge Error
				Triangle &t=triangles[i];vec3f p;
				loopj(0,3) t.err[j]=calculate_error(t.v[j],t.v[(j+1)%3],p);
				t.err[3]=min(t.err[0],min(t.err[1],t.err[2]));
			}
		}

		// Init Reference ID list
		loopi(0,vertices.size())
		{
			vertices[i].tstart=0;
			vertices[i].tcount=0;
		}
		loopi(0,triangles.size())
		{
			Triangle &t=triangles[i];
			loopj(0,3) vertices[t.v[j]].tcount++;
		}
		int tstart=0;
		loopi(0,vertices.size())
		{
			Vertex &v=vertices[i];
			v.tstart=tstart;
			tstart+=v.tcount;
			v.tcount=0;
		}

		// Write References
		refs.resize(triangles.size()*3);
		loopi(0,triangles.size())
		{
			Triangle &t=triangles[i];
			loopj(0,3)
			{
				Vertex &v=vertices[t.v[j]];
				refs[v.tstart+v.tcount].tid=i;
				refs[v.tstart+v.tcount].tvertex=j;
				v.tcount++;
			}
		}

		// Identify boundary : vertices[].border=0,1
		if( iteration == 0 )
		{
			std::vector<int> vcount,vids;

			loopi(0,vertices.size())
				vertices[i].border=0;

			loopi(0,vertices.size())
			{
				Vertex &v=vertices[i];
				vcount.clear();
				vids.clear();
				loopj(0,v.tcount)
				{
					int k=refs[v.tstart+j].tid;
					Triangle &t=triangles[k];
					loopk(0,3)
					{
						int ofs=0,id=t.v[k];
						while(ofs<vcount.size())
						{
							if(vids[ofs]==id)break;
							ofs++;
						}
						if(ofs==vcount.size())
						{
							vcount.push_back(1);
							vids.push_back(id);
						}
						else
							vcount[ofs]++;
					}
				}
				loopj(0,vcount.size()) if(vcount[j]==1)
					vertices[vids[j]].border=1;
			}
		}
	}
