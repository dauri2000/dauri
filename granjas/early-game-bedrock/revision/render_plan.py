from pathlib import Path
import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
from matplotlib.patches import Rectangle, FancyArrowPatch
import nbtlib,sys
from adapt_farm import decode

base=Path(__file__).parent/'output'
f=nbtlib.load(base/'early_game_mob-farm_Bedrock_SD6.litematic')
r=next(iter(f['Regions'].values()));(sx,sy,sz),a=decode(r);pal=r['BlockStatePalette']
fig,axs=plt.subplots(1,3,figsize=(15,7),gridspec_kw={'width_ratios':[1,1.4,1.8]})
fig.patch.set_facecolor('#f6f7fb')
colors={'stone':'#8996a9','slab':'#c2ccdc','D':'#edb26d','O':'#77bfa0','wire':'#cb4863','water':'#89cbed'}
def block(ax,x,y,label,color,h=1):
 ax.add_patch(Rectangle((x,y),1,h,facecolor=color,edgecolor='#283245',linewidth=1))
 if label:ax.text(x+.5,y+h/2,label,ha='center',va='center',fontsize=11,weight='bold',color='#202838')
for F in [34,37,40]:
 block(axs[0],22,F,'D ↑',colors['D']);block(axs[0],22,F+1,'Agua',colors['water']);block(axs[0],22,F+2,'O ↑',colors['O'])
 for x in [21,23]:block(axs[0],x,F+.5,'',colors['slab'],.5)
 axs[0].annotate('',(22.5,F+1.1),(22.5,F+2.05),arrowprops={'arrowstyle':'->','color':'#b82b44','lw':2})
 axs[0].text(23.3,F+1.5,'QC',color='#b82b44',weight='bold')
 for x in [22,24]:block(axs[1],x,F,'',colors['stone'])
 block(axs[1],23,F,'Polvo',colors['wire'],.18)
 block(axs[1],23,F+1,'← D',colors['D']);block(axs[1],23,F+2,'O ↑',colors['O'])
 block(axs[1],22,F+1,'Agua*',colors['water'])
 axs[1].annotate('',(23.5,F+.25),(23.5,F+2.2),arrowprops={'arrowstyle':'->','color':'#17664d','lw':2,'connectionstyle':'arc3,rad=-.7'})
for ax,title in zip(axs[:2],['Original: depende de QC','Adaptación: alimentación directa']):
 ax.set_title(title,pad=17,fontsize=13,weight='bold');ax.set_xlim(20.8,25.5);ax.set_ylim(33.7,43.4);ax.set_aspect('equal');ax.set_xticks([22.5,23.5,24.5],['X22','X23','X24']);ax.set_yticks([34,37,40],['Piso 34','Piso 37','Piso 40']);ax.spines[['top','right']].set_visible(False);ax.set_facecolor('white')
axs[0].text(.5,-.14,'O: observador · D: dispensador',transform=axs[0].transAxes,ha='center',fontsize=9)
axs[1].text(.5,-.14,'*Agua durante el llenado; inicio seco',transform=axs[1].transAxes,ha='center',fontsize=9)
ax=axs[2];F=37
for z in range(15,30):
 for x in range(15,30):
  if abs(x-22)+abs(z-22)>7:continue
  v=a[(F*sz+z)*sx+x];name=str(pal[v]['Name'])
  if name=='minecraft:air':
   block(ax,x,z,'×','#eceff5')
  elif (x,z)==(23,22):block(ax,x,z,'D',colors['D'])
  elif (x,z)==(22,22):block(ax,x,z,'W',colors['water'])
  else:block(ax,x,z,'',colors['stone'] if name=='minecraft:stone' else '#c7dceb')
ax.set_title('Planta de un piso: se mantiene la fuente',pad=17,fontsize=13,weight='bold')
ax.set_xlim(14.3,30);ax.set_ylim(14.3,30);ax.set_aspect('equal');ax.set_xticks([15.5,22.5,29.5],['X15','X22','X29']);ax.set_yticks([15.5,22.5,29.5],['Z15','Z22','Z29']);ax.invert_yaxis();ax.spines[['top','right']].set_visible(False);ax.set_facecolor('white')
ax.text(.5,-.14,'W: fuente original · D: dispensador nuevo\n×: dos puntas secas retiradas por piso',transform=ax.transAxes,ha='center',fontsize=10)
fig.suptitle('Granja de Dalinnar · adaptación Bedrock para simulación ≥ 6',fontsize=17,weight='bold',y=.99)
fig.text(.5,.025,'Esquema técnico de la adaptación. Conserva 13 pisos; revisión estática, sin ensayo en un servidor Bedrock.',ha='center',fontsize=11,color='#45536c')
fig.tight_layout(rect=[0,.16,1,.95]);fig.savefig(base/'COMO-FUNCIONA.png',dpi=150,facecolor=fig.get_facecolor());plt.close(fig)
